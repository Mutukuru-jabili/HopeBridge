package in.hopebridge.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;
    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms}") long expiration) {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiration = expiration;
    }
    public String generate(UserDetails user, String role) {
        return Jwts.builder().subject(user.getUsername()).claim("role", role)
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key).compact();
    }
    public String username(String token) { return claim(token, Claims::getSubject); }
    public boolean valid(String token, UserDetails user) { return user.getUsername().equals(username(token)) && !expired(token); }
    private boolean expired(String token) { return claim(token, Claims::getExpiration).before(new Date()); }
    private <T> T claim(String token, Function<Claims, T> fn) { return fn.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()); }
}
