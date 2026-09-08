package in.hopebridge.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "schemes")
@Getter @Setter @NoArgsConstructor
public class Scheme {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String name;
    @Column(nullable = false, length = 80)
    private String category;
    @Column(nullable = false, length = 120)
    private String provider;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String eligibility;
    @Column(length = 500)
    private String officialUrl;
    private boolean active = true;
}
