package in.hopebridge.web;
import in.hopebridge.model.*;
import in.hopebridge.repository.*;
import in.hopebridge.service.WalletService;
import in.hopebridge.service.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')")
@Transactional
public class AdminController {
 private final CaseRepository cases; private final EvidenceRepository evidence; private final SchemeRepository schemes;
 private final UserRepository users; private final WalletService wallet; private final CurrentUser current;
 public AdminController(CaseRepository c,EvidenceRepository e,SchemeRepository s,UserRepository users,WalletService w,CurrentUser current){
  cases=c; evidence=e; schemes=s; this.users=users; wallet=w; this.current=current;
 }
 @GetMapping("/cases") List<Map<String,Object>> cases(){
  return cases.findAllByOrderByUpdatedAtDesc().stream().map(c->{
   Map<String,Object> m=new HashMap<>(); m.put("id",c.getId()); m.put("title",c.getTitle());
   m.put("category",c.getCategory()); m.put("status",c.getStatus().name());
   m.put("applicant",c.getApplicant().getFullName()); m.put("email",c.getApplicant().getEmail());
   m.put("updatedAt",c.getUpdatedAt());
   m.put("assignedReviewer", c.getAssignedReviewer() == null ? null : reviewerView(c.getAssignedReviewer()));
   return m;
  }).toList();
 }
 @GetMapping("/reviewers") List<Map<String,Object>> reviewers(){
  return users.findByRoleOrderByFullNameAsc(Role.ADMIN).stream().map(this::reviewerView).toList();
 }
 @PatchMapping("/cases/{id}/assign")
 Map<String,Object> assign(@PathVariable Long id,@RequestParam Long reviewerId,Authentication authentication){
  CaseFile c=cases.findById(id).orElseThrow();
  User reviewer=users.findById(reviewerId).orElseThrow();
  if(reviewer.getRole()!=Role.ADMIN) throw new IllegalArgumentException("Cases can only be assigned to an admin reviewer.");
  if(c.getApplicant().getId().equals(reviewer.getId())) throw new IllegalArgumentException("An applicant cannot review their own case.");
  c.setAssignedReviewer(reviewer);
  if(c.getStatus()==CaseStatus.SUBMITTED || c.getStatus()==CaseStatus.ACTION_REQUIRED) c.setStatus(CaseStatus.UNDER_REVIEW);
  c.setUpdatedAt(Instant.now());
  cases.save(c);
  return Map.of("id",c.getId(),"status",c.getStatus().name(),"assignedReviewer",reviewerView(reviewer));
 }
 @DeleteMapping("/cases/{id}/assign")
 Map<String,Object> unassign(@PathVariable Long id){
  CaseFile c=cases.findById(id).orElseThrow(); c.setAssignedReviewer(null); c.setUpdatedAt(Instant.now()); cases.save(c);
  return Map.of("id",c.getId(),"status",c.getStatus().name(),"assignedReviewer","");
 }
 @PatchMapping("/cases/{id}")
 Map<String,Object> review(@PathVariable Long id,@RequestParam CaseStatus status,
   @RequestParam(required=false,defaultValue="") String note,Authentication authentication){
  CaseFile c=cases.findById(id).orElseThrow(); User reviewer=current.get(authentication);
  if(c.getApplicant().getId().equals(reviewer.getId())) throw new IllegalArgumentException("Self-review is not permitted.");
  if(c.getAssignedReviewer()!=null && !c.getAssignedReviewer().getId().equals(reviewer.getId()))
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This case is assigned to another reviewer.");
  if(status==CaseStatus.APPROVED && (c.getAssignedReviewer()==null || !c.getAssignedReviewer().getId().equals(reviewer.getId())))
   throw new IllegalArgumentException("Approval requires an assigned independent reviewer.");
  c.setStatus(status); c.setAdminNote(note); c.setUpdatedAt(Instant.now());
  return Map.of("id",cases.save(c).getId(),"status",c.getStatus().name(),"message","Review decision recorded.");
 }
 @GetMapping("/evidence") List<Map<String,Object>> evidence(){
  return this.evidence.findAll().stream().map(this::evidenceView).toList();
 }
 @GetMapping("/cases/{caseId}/evidence") List<Map<String,Object>> caseEvidence(@PathVariable Long caseId){
  CaseFile c=cases.findById(caseId).orElseThrow();
  return evidence.findByCaseFileOrderByUploadedAtDesc(c).stream().map(this::evidenceView).toList();
 }
 @PatchMapping("/evidence/{id}")
 Map<String,Object> evidenceReview(@PathVariable Long id,@RequestParam EvidenceStatus status,
   @RequestParam(required=false,defaultValue="") String note,Authentication authentication){
  Evidence e=evidence.findById(id).orElseThrow(); CaseFile c=e.getCaseFile(); User reviewer=current.get(authentication);
  if(c.getAssignedReviewer()!=null && !c.getAssignedReviewer().getId().equals(reviewer.getId()))
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This case is assigned to another reviewer.");
  EvidenceStatus previous=e.getStatus(); e.setStatus(status); e.setReviewNote(note); evidence.save(e);
  if(status==EvidenceStatus.VERIFIED && previous!=EvidenceStatus.VERIFIED) wallet.award(c.getApplicant(),5,"Evidence verified");
  return Map.of("id",e.getId(),"status",e.getStatus().name());
 }
 @PostMapping("/schemes") Scheme addScheme(@RequestBody Scheme scheme){scheme.setId(null);return schemes.save(scheme);}
 private Map<String,Object> reviewerView(User u){return Map.of("id",u.getId(),"fullName",u.getFullName(),"email",u.getEmail(),"role",u.getRole().name());}
 private Map<String,Object> evidenceView(Evidence e){
  return Map.of("id",e.getId(),"caseId",e.getCaseFile().getId(),"fileName",e.getFileName(),
    "contentType",e.getContentType(),"sizeBytes",e.getSizeBytes(),"status",e.getStatus().name(),
    "reviewNote",e.getReviewNote()==null?"":e.getReviewNote(),"uploadedAt",e.getUploadedAt());
 }
}
