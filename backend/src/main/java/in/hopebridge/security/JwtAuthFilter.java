package in.hopebridge.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt; private final CustomUserDetailsService users;
    public JwtAuthFilter(JwtService jwt, CustomUserDetailsService users) { this.jwt = jwt; this.users = users; }
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, java.io.IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7), email = jwt.username(token);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails details = users.loadUserByUsername(email);
                    if (jwt.valid(token, details)) SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
                }
            } catch (RuntimeException ignored) { /* invalid tokens are treated as anonymous */ }
        }
        chain.doFilter(request, response);
    }
}
