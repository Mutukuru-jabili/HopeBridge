package in.hopebridge.repository;
import in.hopebridge.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {
    List<RewardTransaction> findByUserOrderByCreatedAtDesc(User user);
}
