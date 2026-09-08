package in.hopebridge.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "reward_transactions")
@Getter @Setter @NoArgsConstructor
public class RewardTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private User user;
    private int points;
    @Column(nullable = false, length = 180) private String reason;
    @ManyToOne(fetch = FetchType.LAZY) private User admin;
    @ManyToOne(fetch = FetchType.LAZY) private CaseFile caseFile;
    @ManyToOne(fetch = FetchType.LAZY) private Evidence evidence;
    @Column(nullable = false, length = 30) private String type = "EVIDENCE_APPROVAL";
    private Instant createdAt = Instant.now();
}
