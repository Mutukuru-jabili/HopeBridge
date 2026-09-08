package in.hopebridge.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private User admin;
    @Column(nullable = false, length = 80) private String action;
    @Column(nullable = false, length = 80) private String entityType;
    @Column(nullable = false) private Long entityId;
    private Long userId;
    private Integer points;
    @Column(columnDefinition = "TEXT") private String remarks;
    @Column(nullable = false) private Instant createdAt = Instant.now();
}
