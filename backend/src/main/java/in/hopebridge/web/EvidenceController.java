package in.hopebridge.web;

import in.hopebridge.model.*;
import in.hopebridge.repository.*;
import in.hopebridge.service.CurrentUser;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@RestController @RequestMapping("/api") @Transactional
public class EvidenceController {
    private final CaseRepository cases; private final EvidenceRepository evidence; private final CurrentUser current;
    private static final Set<String> TYPES=Set.of("image/jpeg","image/png");
    public EvidenceController(CaseRepository cases, EvidenceRepository evidence, CurrentUser current) { this.cases=cases;this.evidence=evidence;this.current=current; }
    @PostMapping("/cases/{caseId}/evidence") ResponseEntity<?> upload(@PathVariable Long caseId,@RequestParam("file") MultipartFile file,
        @RequestParam(defaultValue="0") int requestedPoints, Authentication a) throws Exception {
        CaseFile c=cases.findById(caseId).orElseThrow(); if(!c.getApplicant().getId().equals(current.get(a).getId())) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        if(file.isEmpty() || file.getSize()>10*1024*1024 || !TYPES.contains(file.getContentType())) return ResponseEntity.badRequest().body(Map.of("message","Only non-empty JPG or PNG files up to 10 MB are accepted."));
        if (requestedPoints < 0 || requestedPoints > 10000) return ResponseEntity.badRequest().body(Map.of("message","Requested points must be between 0 and 10,000."));
        Evidence e=new Evidence();e.setCaseFile(c);e.setRequestedPoints(requestedPoints);e.setFileName(file.getOriginalFilename()==null?"upload":file.getOriginalFilename());e.setContentType(file.getContentType());e.setSizeBytes(file.getSize());e.setContent(file.getBytes());return ResponseEntity.status(201).body(Map.of("id",evidence.save(e).getId(),"message","Evidence uploaded and awaiting independent review."));
    }
    @GetMapping("/evidence/{id}/download") ResponseEntity<byte[]> download(@PathVariable Long id,Authentication a) {
        Evidence e=evidence.findById(id).orElseThrow();
        User user=current.get(a);
        boolean admin=a.getAuthorities().stream().anyMatch(x -> "ROLE_ADMIN".equals(x.getAuthority()));
        if(!admin && !e.getCaseFile().getApplicant().getId().equals(user.getId()))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        String fileName=e.getFileName().replace("\"","").replace("\r","").replace("\n","");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(e.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+fileName+"\"").body(e.getContent());
    }
}
