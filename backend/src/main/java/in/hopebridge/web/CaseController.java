package in.hopebridge.web;

import in.hopebridge.model.*;
import in.hopebridge.repository.*;
import in.hopebridge.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/cases") @Transactional
public class CaseController {
    private final CaseRepository cases; private final CurrentUser current; private final WalletService wallet;
    public CaseController(CaseRepository cases, CurrentUser current, WalletService wallet) { this.cases=cases; this.current=current; this.wallet=wallet; }
    record CaseRequest(@NotBlank String title, @NotBlank String category, @NotBlank String description, String district) {}
    @GetMapping List<Map<String,Object>> list(Authentication a) {
        User u=current.get(a);
        return isAdmin(a) ? cases.findAllByOrderByUpdatedAtDesc().stream().map(this::view).toList()
                : cases.findByApplicantOrderByUpdatedAtDesc(u).stream().map(this::view).toList();
    }
    @GetMapping("/{id}") Map<String,Object> one(@PathVariable Long id, Authentication a) {
        User u=current.get(a); return view(isAdmin(a) ? cases.findById(id).orElseThrow() : owned(id,u));
    }
    @GetMapping("/{id}/evidence") List<Map<String,Object>> evidence(@PathVariable Long id, Authentication a) {
        User u=current.get(a); CaseFile c=isAdmin(a) ? cases.findById(id).orElseThrow() : owned(id,u);
        return c.getEvidence().stream().map(e -> Map.<String,Object>of("id",e.getId(),"fileName",e.getFileName(),
                "contentType",e.getContentType(),"sizeBytes",e.getSizeBytes(),"status",e.getStatus().name(),
                "reviewNote",e.getReviewNote()==null?"":e.getReviewNote(),"uploadedAt",e.getUploadedAt())).toList();
    }
    @PostMapping ResponseEntity<?> create(@Valid @RequestBody CaseRequest r, Authentication a) { User u=current.get(a); CaseFile c = new CaseFile(); apply(c,r); c.setApplicant(u); c=cases.save(c); return ResponseEntity.status(201).body(view(c)); }
    @PutMapping("/{id}") Map<String,Object> update(@PathVariable Long id, @Valid @RequestBody CaseRequest r, Authentication a) { CaseFile c=owned(id,current.get(a)); if (c.getStatus()!=CaseStatus.DRAFT && c.getStatus()!=CaseStatus.ACTION_REQUIRED) throw new IllegalStateException("Only draft or action-required cases can be edited."); apply(c,r); c.setUpdatedAt(Instant.now()); return view(cases.save(c)); }
    @PostMapping("/{id}/submit") Map<String,Object> submit(@PathVariable Long id, Authentication a) { CaseFile c=owned(id,current.get(a)); if(c.getStatus()!=CaseStatus.DRAFT && c.getStatus()!=CaseStatus.ACTION_REQUIRED) throw new IllegalStateException("Case cannot be submitted in its current status."); c.setStatus(CaseStatus.SUBMITTED); c.setUpdatedAt(Instant.now()); wallet.award(c.getApplicant(),10,"Case submitted for review"); return view(cases.save(c)); }
    @DeleteMapping("/{id}") ResponseEntity<?> delete(@PathVariable Long id, Authentication a) { CaseFile c=owned(id,current.get(a)); if(c.getStatus()!=CaseStatus.DRAFT) throw new IllegalStateException("Only drafts can be deleted."); cases.delete(c); return ResponseEntity.noContent().build(); }
    private void apply(CaseFile c, CaseRequest r) { c.setTitle(r.title()); c.setCategory(r.category()); c.setDescription(r.description()); c.setDistrict(r.district()); }
    private CaseFile owned(Long id, User u) { CaseFile c=cases.findById(id).orElseThrow(); if(!c.getApplicant().getId().equals(u.getId())) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND); return c; }
    Map<String,Object> view(CaseFile c) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("id",c.getId()); result.put("title",c.getTitle()); result.put("category",c.getCategory());
        result.put("description",c.getDescription()); result.put("district",c.getDistrict()==null?"":c.getDistrict());
        result.put("status",c.getStatus().name()); result.put("adminNote",c.getAdminNote()==null?"":c.getAdminNote());
        result.put("createdAt",c.getCreatedAt()); result.put("updatedAt",c.getUpdatedAt());
        result.put("assignedReviewer",c.getAssignedReviewer()==null?"":Map.of(
                "id",c.getAssignedReviewer().getId(),"fullName",c.getAssignedReviewer().getFullName()));
        result.put("evidence",c.getEvidence().stream().map(e->{Map<String,Object> m=new LinkedHashMap<>();
                m.put("id",e.getId());m.put("fileName",e.getFileName());m.put("contentType",e.getContentType());
                m.put("sizeBytes",e.getSizeBytes());m.put("status",e.getStatus().name());
                m.put("requestedPoints",e.getRequestedPoints());m.put("approvedPoints",e.getApprovedPoints());
                m.put("reviewNote",e.getReviewNote()==null?"":e.getReviewNote());m.put("reviewedAt",e.getReviewedAt());return m;}).toList());
        return result;
    }
    private boolean isAdmin(Authentication a) {
        return a.getAuthorities().stream().anyMatch(x -> "ROLE_ADMIN".equals(x.getAuthority()));
    }
}
