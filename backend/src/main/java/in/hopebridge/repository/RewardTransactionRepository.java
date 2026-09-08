package in.hopebridge.repository;
import in.hopebridge.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {
    List<RewardTransaction> findByUserOrderByCreatedAtDesc(User user);
    @Query("select coalesce(sum(t.points), 0) from RewardTransaction t where t.type in ('EVIDENCE_APPROVED', 'EVIDENCE_APPROVAL')")
    long sumEvidencePoints();
}
