package in.hopebridge.web;

import in.hopebridge.model.Scheme;
import in.hopebridge.model.CaseFile;
import in.hopebridge.model.User;
import in.hopebridge.repository.CaseRepository;
import in.hopebridge.repository.SchemeRepository;
import in.hopebridge.service.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/schemes")
public class SchemeController {
    private final SchemeRepository schemes; private final CaseRepository cases; private final CurrentUser current;
    public SchemeController(SchemeRepository schemes, CaseRepository cases, CurrentUser current){this.schemes=schemes;this.cases=cases;this.current=current;}
    @GetMapping List<Scheme> list(@RequestParam(required=false) String category){return category==null||category.isBlank()?schemes.findByActiveTrueOrderByNameAsc():schemes.findByActiveTrueAndCategoryIgnoreCaseOrderByNameAsc(category);}
    @GetMapping("/recommend") List<Scheme> recommend(@RequestParam(required=false) String category,
            @RequestParam(required=false) String keywords,@RequestParam(required=false) Long caseId,Authentication authentication){
        if(caseId!=null) return recommendForCase(caseId, authentication);
        List<Scheme> all=list(category);
        if(keywords==null||keywords.isBlank()) return all;
        String q=keywords.toLowerCase(Locale.ROOT);
        return all.stream().filter(s->searchText(s).contains(q)).toList();
    }
    @GetMapping({"/recommend/case/{caseId}","/recommend/{caseId}","/by-case/{caseId}"})
    List<Scheme> recommendForCase(@PathVariable Long caseId,Authentication authentication){
        if(authentication==null || !authentication.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        User user=current.get(authentication);
        CaseFile c=cases.findById(caseId).orElseThrow();
        boolean admin=authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if(!admin && !c.getApplicant().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        String query=(c.getCategory()+" "+c.getTitle()+" "+c.getDescription()).toLowerCase(Locale.ROOT);
        return list(null).stream().sorted(Comparator.comparingInt((Scheme s)->score(s,query)).reversed())
                .filter(s -> score(s,query)>0).toList();
    }
    @GetMapping("/{id}") Scheme detail(@PathVariable Long id){
        return schemes.findById(id).filter(Scheme::isActive).orElseThrow();
    }
    private int score(Scheme s,String query){
        int score=0;
        if(s.getCategory()!=null && query.contains(s.getCategory().toLowerCase(Locale.ROOT))) score+=5;
        if(s.getName()!=null && query.contains(s.getName().toLowerCase(Locale.ROOT))) score+=3;
        for(String word:query.split("\\W+")) if(word.length()>3 && searchText(s).contains(word)) score++;
        return score;
    }
    private String searchText(Scheme s){return (Objects.toString(s.getName(),"")+" "+Objects.toString(s.getDescription(),"")+" "+Objects.toString(s.getEligibility(),"")+" "+Objects.toString(s.getCategory(),"")).toLowerCase(Locale.ROOT);}
}
