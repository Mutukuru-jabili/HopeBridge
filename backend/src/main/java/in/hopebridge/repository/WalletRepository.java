package in.hopebridge.repository;
import in.hopebridge.model.*;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WalletRepository extends JpaRepository<Wallet, Long> { Optional<Wallet> findByUser(User user); }
