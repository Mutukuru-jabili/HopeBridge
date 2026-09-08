package in.hopebridge.web;

import in.hopebridge.model.User;
import in.hopebridge.repository.UserRepository;
import in.hopebridge.service.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@Transactional
public class UserController {
    private final UserRepository users;
    private final CurrentUser current;

    public UserController(UserRepository users, CurrentUser current) {
        this.users = users;
        this.current = current;
    }

    record ProfileUpdate(@Size(max = 120) String fullName, @Size(max = 20) String phone) {}

    @GetMapping({"/api/users/me", "/api/profile", "/api/auth/me"})
    Map<String,Object> me(Authentication authentication) {
        return AuthController.userProfile(current.get(authentication));
    }

    @PatchMapping({"/api/users/me", "/api/profile", "/api/auth/me"})
    Map<String,Object> update(@Valid @RequestBody ProfileUpdate update, Authentication authentication) {
        User user = current.get(authentication);
        if (update.fullName() != null) {
            String fullName = update.fullName().trim();
            if (fullName.isBlank()) throw new IllegalArgumentException("Full name cannot be blank.");
            user.setFullName(fullName);
        }
        if (update.phone() != null) user.setPhone(update.phone().trim());
        return AuthController.userProfile(users.save(user));
    }
}
