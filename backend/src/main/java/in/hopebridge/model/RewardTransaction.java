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
    private Instant createdAt = Instant.now();
}
