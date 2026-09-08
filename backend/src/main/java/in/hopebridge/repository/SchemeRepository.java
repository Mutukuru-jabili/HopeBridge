package in.hopebridge.repository;
import in.hopebridge.model.Scheme;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SchemeRepository extends JpaRepository<Scheme, Long> { List<Scheme> findByActiveTrueOrderByNameAsc(); List<Scheme> findByActiveTrueAndCategoryIgnoreCaseOrderByNameAsc(String category); }
