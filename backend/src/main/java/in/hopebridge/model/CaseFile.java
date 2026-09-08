package in.hopebridge.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "cases")
@Getter @Setter @NoArgsConstructor
public class CaseFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, length = 80)
    private String category;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(length = 120)
    private String district;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private CaseStatus status = CaseStatus.DRAFT;
    @Column(columnDefinition = "TEXT")
    private String adminNote;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User applicant;
    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedReviewer;
    @OneToMany(mappedBy = "caseFile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evidence> evidence = new ArrayList<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
