package in.hopebridge.repository;
import in.hopebridge.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CaseRepository extends JpaRepository<CaseFile, Long> {
    List<CaseFile> findByApplicantOrderByUpdatedAtDesc(User applicant);
    List<CaseFile> findAllByOrderByUpdatedAtDesc();
    long countByStatus(CaseStatus status);
}
