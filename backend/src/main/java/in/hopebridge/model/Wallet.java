package in.hopebridge.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "wallets")
@Getter @Setter @NoArgsConstructor
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false) @JoinColumn(unique = true)
    private User user;
    private int balance;
    private Instant updatedAt = Instant.now();
}
