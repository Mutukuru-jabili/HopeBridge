package in.hopebridge.repository;
import in.hopebridge.model.Evidence;
import in.hopebridge.model.CaseFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    List<Evidence> findByCaseFileOrderByUploadedAtDesc(CaseFile caseFile);
}
