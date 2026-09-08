package in.hopebridge.repository;
import in.hopebridge.model.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRoleOrderByFullNameAsc(in.hopebridge.model.Role role);
}
