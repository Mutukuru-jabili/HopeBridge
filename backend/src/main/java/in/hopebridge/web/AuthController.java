package in.hopebridge.web;

import in.hopebridge.model.*;
import in.hopebridge.repository.*;
import in.hopebridge.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final WalletRepository wallets; private final PasswordEncoder encoder; private final AuthenticationManager auth; private final JwtService jwt;
    public AuthController(UserRepository users, WalletRepository wallets, PasswordEncoder encoder, AuthenticationManager auth, JwtService jwt) { this.users=users; this.wallets=wallets; this.encoder=encoder; this.auth=auth; this.jwt=jwt; }
    record Register(@NotBlank String fullName, @Email @NotBlank String email, @Size(min=8) String password, String phone) {}
    record Login(@Email @NotBlank String email, @NotBlank String password) {}
    @PostMapping("/register") ResponseEntity<?> register(@Valid @RequestBody Register r) {
        if (users.existsByEmailIgnoreCase(r.email())) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","An account with this email already exists."));
        User u = new User(); u.setFullName(r.fullName()); u.setEmail(r.email().toLowerCase(Locale.ROOT)); u.setPassword(encoder.encode(r.password())); u.setPhone(r.phone()); u = users.save(u);
        Wallet w = new Wallet(); w.setUser(u); wallets.save(w);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile(u, jwt.generate(details(u), u.getRole().name())));
    }
    @PostMapping("/login") ResponseEntity<?> login(@Valid @RequestBody Login l) {
        auth.authenticate(new UsernamePasswordAuthenticationToken(l.email(), l.password()));
        User u = users.findByEmailIgnoreCase(l.email()).orElseThrow();
        return ResponseEntity.ok(profile(u, jwt.generate(details(u), u.getRole().name())));
    }
    private UserDetails details(User u) { return org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password(u.getPassword()).roles(u.getRole().name()).build(); }
    static Map<String,Object> profile(User u, String token) {
        Map<String,Object> user = userProfile(u);
        return Map.of("token",token,"user",user);
    }
    static Map<String,Object> userProfile(User u) {
        return Map.of("id",u.getId(),"fullName",u.getFullName(),"email",u.getEmail(),
                "phone",u.getPhone() == null ? "" : u.getPhone(),"role",u.getRole().name());
    }
}
