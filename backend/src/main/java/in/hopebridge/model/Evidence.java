package in.hopebridge.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "evidence")
@Getter @Setter @NoArgsConstructor
public class Evidence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CaseFile caseFile;
    @Column(nullable = false, length = 180)
    private String fileName;
    @Column(nullable = false, length = 100)
    private String contentType;
    @Column(nullable = false)
    private long sizeBytes;
    @Lob @Basic(fetch = FetchType.LAZY) @Column(columnDefinition = "LONGBLOB")
    private byte[] content;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EvidenceStatus status = EvidenceStatus.PENDING;
    @Column(columnDefinition = "TEXT")
    private String reviewNote;
    @Column(nullable = false)
    private int requestedPoints = 0;
    private Integer approvedPoints;
    @ManyToOne(fetch = FetchType.LAZY)
    private User reviewedBy;
    private Instant reviewedAt;
    private Instant uploadedAt = Instant.now();
}
