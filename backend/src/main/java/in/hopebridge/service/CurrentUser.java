package in.hopebridge.service;

import in.hopebridge.model.User;
import in.hopebridge.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final UserRepository users;
    public CurrentUser(UserRepository users) { this.users = users; }
    public User get(Authentication auth) { return users.findByEmailIgnoreCase(auth.getName()).orElseThrow(); }
}
