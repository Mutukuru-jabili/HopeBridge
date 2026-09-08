package in.hopebridge.service;

import in.hopebridge.model.*;
import in.hopebridge.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedData {
    @Bean CommandLineRunner seed(SchemeRepository schemes, UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (schemes.count() == 0) {
                Scheme s = new Scheme(); s.setName("PM-KISAN Samman Nidhi"); s.setCategory("Agriculture"); s.setProvider("Government of India");
                s.setDescription("Income support for eligible landholding farmer families."); s.setEligibility("Small and marginal farmers with cultivable land."); s.setOfficialUrl("https://pmkisan.gov.in"); schemes.save(s);
                s = new Scheme(); s.setName("Ayushman Bharat PM-JAY"); s.setCategory("Healthcare"); s.setProvider("National Health Authority");
                s.setDescription("Cashless health cover for eligible families at empanelled hospitals."); s.setEligibility("Families identified in the SECC database."); s.setOfficialUrl("https://pmjay.gov.in"); schemes.save(s);
                s = new Scheme(); s.setName("National Scholarship Portal"); s.setCategory("Education"); s.setProvider("Government of India");
                s.setDescription("Discover and apply for scholarships across departments."); s.setEligibility("Eligibility varies by scholarship and education level."); s.setOfficialUrl("https://scholarships.gov.in"); schemes.save(s);
            }
            if (users.count() == 0) {
                User admin = new User(); admin.setEmail("admin@hopebridge.in"); admin.setFullName("HopeBridge Admin"); admin.setPassword(encoder.encode("Admin@12345")); admin.setRole(Role.ADMIN); users.save(admin);
            }
        };
    }
}
