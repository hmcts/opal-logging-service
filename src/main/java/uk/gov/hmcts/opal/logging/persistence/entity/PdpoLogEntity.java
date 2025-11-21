package uk.gov.hmcts.opal.logging.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.dialect.PostgreSQLInetJdbcType;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;

@Entity
@Table(name = "pdpo_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PdpoLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pdpo_log_seq")
    @SequenceGenerator(name = "pdpo_log_seq", sequenceName = "pdpo_log_seq", allocationSize = 1)
    @Column(name = "pdpo_log_id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "created_by_identifier", nullable = false, length = 50)
    private String createdByIdentifier;

    @Column(name = "created_by_identifier_type", nullable = false, length = 30)
    private String createdByIdentifierType;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @JdbcType(PostgreSQLInetJdbcType.class)
    @Column(name = "ip_address", nullable = false, columnDefinition = "INET")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "category", nullable = false, columnDefinition = "pl_category_enum")
    private PdpoCategory category;

    @Column(name = "recipient_identifier", length = 50)
    private String recipientIdentifier;

    @Column(name = "recipient_identifier_type", length = 30)
    private String recipientIdentifierType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pdpo_identifiers_id")
    private PdpoIdentifierEntity businessIdentifier;

    @Builder.Default
    @OneToMany(
        mappedBy = "log",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PdpoLogIndividualEntity> individuals = new ArrayList<>();

    public void addIndividual(PdpoLogIndividualEntity individual) {
        individuals.add(individual);
        individual.setLog(this);
    }
}
