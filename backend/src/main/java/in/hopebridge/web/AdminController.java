package in.hopebridge.web;

import in.hopebridge.model.*;
import in.hopebridge.repository.*;
import in.hopebridge.service.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public class AdminController {
    private final CaseRepository cases;
    private final EvidenceRepository evidence;
    private final SchemeRepository schemes;
    private final UserRepository users;
    private final WalletService wallet;
    private final CurrentUser current;
    private final AuditLogRepository audit;
    private final RewardTransactionRepository transactions;

    public AdminController(CaseRepository cases, EvidenceRepository evidence, SchemeRepository schemes,
        UserRepository users, WalletService wallet, CurrentUser current, AuditLogRepository audit,
        RewardTransactionRepository transactions) {
        this.cases = cases; this.evidence = evidence; this.schemes = schemes; this.users = users;
        this.wallet = wallet; this.current = current; this.audit = audit; this.transactions = transactions;
    }

    @GetMapping("/cases")
    List<Map<String,Object>> cases() {
        return cases.findAllByOrderByUpdatedAtDesc().stream().map(c -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", c.getId()); m.put("title", c.getTitle()); m.put("category", c.getCategory());
            m.put("status", c.getStatus().name()); m.put("applicant", c.getApplicant().getFullName());
            m.put("email", c.getApplicant().getEmail()); m.put("updatedAt", c.getUpdatedAt());
            return m;
        }).toList();
    }

    @GetMapping("/evidence/pending")
    List<Map<String,Object>> pendingEvidence() {
        return evidence.findByStatusOrderByUploadedAtAsc(EvidenceStatus.PENDING).stream().map(this::evidenceView).toList();
    }

    @GetMapping("/overview")
    Map<String,Object> overview() {
        long pending = evidence.findByStatusOrderByUploadedAtAsc(EvidenceStatus.PENDING).size();
        long approved = evidence.findAll().stream().filter(e -> e.getStatus() == EvidenceStatus.APPROVED).count();
        long rejected = evidence.findAll().stream().filter(e -> e.getStatus() == EvidenceStatus.REJECTED).count();
        return Map.of("users", users.count(), "cases", cases.count(), "pendingEvidence", pending,
            "approvedEvidence", approved, "rejectedEvidence", rejected);
    }

    @GetMapping("/dashboard")
    Map<String,Object> dashboard() { return overview(); }

    @GetMapping("/evidence")
    List<Map<String,Object>> allEvidence() {
        return evidence.findAll().stream().map(this::evidenceView).toList();
    }

    @GetMapping("/evidence/{id}")
    Map<String,Object> evidence(@PathVariable Long id) {
        return evidenceView(evidence.findById(id).orElseThrow());
    }

    @PutMapping("/evidence/{id}/approve")
    Map<String,Object> approve(@PathVariable Long id, @RequestBody ApproveRequest request, Authentication authentication) {
        Evidence item = evidence.findById(id).orElseThrow();
        if (item.getStatus() != EvidenceStatus.PENDING) throw new IllegalStateException("Only pending evidence can be approved.");
        User admin = current.get(authentication);
        if (item.getCaseFile().getApplicant().getId().equals(admin.getId())) throw new IllegalArgumentException("Users cannot approve their own evidence.");
        int points = request.approvedPoints() == null ? item.getRequestedPoints() : request.approvedPoints();
        if (points <= 0 || points > 10000) throw new IllegalArgumentException("Approved points must be between 1 and 10,000.");
        item.setStatus(EvidenceStatus.APPROVED); item.setApprovedPoints(points);
        item.setReviewNote(request.remarks()); item.setReviewedBy(admin); item.setReviewedAt(Instant.now());
        evidence.save(item);
        wallet.award(item.getCaseFile().getApplicant(), points, "Evidence approved: " + item.getFileName(),
            admin, item.getCaseFile(), item, "EVIDENCE_APPROVED");
        log(admin, "APPROVE_EVIDENCE", "EVIDENCE", id, item.getCaseFile().getApplicant().getId(), points, request.remarks());
        return evidenceView(item);
    }

    @PutMapping("/evidence/{id}/reject")
    Map<String,Object> reject(@PathVariable Long id, @RequestBody RejectRequest request, Authentication authentication) {
        Evidence item = evidence.findById(id).orElseThrow();
        if (item.getStatus() != EvidenceStatus.PENDING) throw new IllegalStateException("Only pending evidence can be rejected.");
        if (request.reason() == null || request.reason().isBlank()) throw new IllegalArgumentException("A rejection reason is required.");
        User admin = current.get(authentication);
        item.setStatus(EvidenceStatus.REJECTED); item.setReviewNote(request.reason());
        item.setReviewedBy(admin); item.setReviewedAt(Instant.now()); evidence.save(item);
        log(admin, "REJECT_EVIDENCE", "EVIDENCE", id, item.getCaseFile().getApplicant().getId(), null, request.reason());
        return evidenceView(item);
    }

    @GetMapping("/rewards")
    List<Map<String,Object>> rewards() {
        return transactions.findAll().stream().sorted(Comparator.comparing(RewardTransaction::getCreatedAt).reversed())
            .map(t -> {
                Map<String,Object> m = new HashMap<>();
                m.put("id", t.getId()); m.put("userId", t.getUser().getId()); m.put("user", t.getUser().getFullName());
                m.put("evidenceId", t.getEvidence() == null ? null : t.getEvidence().getId());
                m.put("points", t.getPoints()); m.put("reason", t.getReason()); m.put("type", t.getType());
                m.put("createdAt", t.getCreatedAt()); return m;
            }).toList();
    }

    @PutMapping("/rewards/{userId}/adjust")
    Map<String,Object> adjust(@PathVariable Long userId, @RequestBody AdjustRequest request, Authentication authentication) {
        if (request.points() == 0) throw new IllegalArgumentException("Adjustment cannot be zero.");
        if (request.reason() == null || request.reason().isBlank()) throw new IllegalArgumentException("A reason is required.");
        User admin = current.get(authentication);
        User user = users.findById(userId).orElseThrow();
        wallet.adjust(user, request.points(), request.reason(), admin);
        log(admin, "ADJUST_REWARD", "USER", userId, userId, request.points(), request.reason());
        return Map.of("userId", userId, "balance", wallet.wallet(user).getBalance());
    }

    @GetMapping("/users")
    List<Map<String,Object>> users() {
        return users.findAll().stream().map(u -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", u.getId()); m.put("fullName", u.getFullName());
            m.put("email", u.getEmail()); m.put("role", u.getRole().name()); return m;
        }).toList();
    }

    @PostMapping("/schemes")
    Scheme addScheme(@RequestBody Scheme scheme) { scheme.setId(null); return schemes.save(scheme); }

    public record ApproveRequest(Integer approvedPoints, String remarks) {}
    public record RejectRequest(String reason) {}
    public record AdjustRequest(int points, String reason) {}

    private void log(User admin, String action, String type, Long id, String remarks) {
        log(admin, action, type, id, null, null, remarks);
    }
    private void log(User admin, String action, String type, Long id, Long userId, Integer points, String remarks) {
        AuditLog entry = new AuditLog(); entry.setAdmin(admin); entry.setAction(action);
        entry.setEntityType(type); entry.setEntityId(id); entry.setUserId(userId); entry.setPoints(points);
        entry.setRemarks(remarks); audit.save(entry);
    }

    private Map<String,Object> evidenceView(Evidence e) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", e.getId()); m.put("caseId", e.getCaseFile().getId());
        m.put("caseTitle", e.getCaseFile().getTitle()); m.put("userId", e.getCaseFile().getApplicant().getId());
        m.put("user", e.getCaseFile().getApplicant().getFullName()); m.put("email", e.getCaseFile().getApplicant().getEmail());
        m.put("description", e.getCaseFile().getDescription()); m.put("fileName", e.getFileName());
        m.put("contentType", e.getContentType()); m.put("status", e.getStatus().name());
        m.put("requestedPoints", e.getRequestedPoints()); m.put("approvedPoints", e.getApprovedPoints());
        m.put("reviewNote", e.getReviewNote() == null ? "" : e.getReviewNote());
        m.put("uploadedAt", e.getUploadedAt()); m.put("reviewedAt", e.getReviewedAt());
        return m;
    }
}
