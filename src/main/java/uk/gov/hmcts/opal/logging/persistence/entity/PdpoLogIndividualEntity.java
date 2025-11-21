package uk.gov.hmcts.opal.logging.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pdpo_log_individuals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PdpoLogIndividualEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pdpo_log_individuals_seq")
    @SequenceGenerator(
        name = "pdpo_log_individuals_seq",
        sequenceName = "pdpo_log_individuals_seq",
        allocationSize = 1
    )
    @Column(name = "pdpo_log_individuals_id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "individual_identifier", nullable = false, length = 50)
    private String individualIdentifier;

    @Column(name = "individual_type", nullable = false, length = 30)
    private String individualType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdpo_log_id", nullable = false)
    private PdpoLogEntity log;
}
