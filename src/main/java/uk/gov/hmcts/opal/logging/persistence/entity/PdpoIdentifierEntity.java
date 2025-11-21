package uk.gov.hmcts.opal.logging.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pdpo_identifiers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PdpoIdentifierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pdpo_identifiers_seq")
    @SequenceGenerator(name = "pdpo_identifiers_seq", sequenceName = "pdpo_identifiers_seq", allocationSize = 1)
    @Column(name = "pdpo_identifiers_id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "business_identifier", nullable = false, length = 250)
    private String businessIdentifier;
}
