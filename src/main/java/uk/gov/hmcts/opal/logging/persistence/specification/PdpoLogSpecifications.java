package uk.gov.hmcts.opal.logging.persistence.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogIndividualEntity;

/**
 * Factory for {@link Specification} instances used to query {@link PdpoLogEntity}.
 */
@Component
public class PdpoLogSpecifications extends EntitySpecs<PdpoLogEntity> {

    public Specification<PdpoLogEntity> findBySearchCriteria(PdpoLogSearchCriteria criteria) {
        return Specification.allOf(specificationList(
            createdByFilter(criteria),
            notBlank(criteria.businessIdentifier()).map(PdpoLogSpecifications::businessIdentifier),
            individualFilterFromCriteria(criteria),
            notNull(criteria.category()).map(PdpoLogSpecifications::category),
            notNull(criteria.createdAfter()).map(PdpoLogSpecifications::createdAfter)));
    }

    private Optional<Specification<PdpoLogEntity>> createdByFilter(PdpoLogSearchCriteria criteria) {
        return notBlank(criteria.createdByIdentifier())
            .flatMap(identifier -> notBlank(criteria.createdByType())
                .map(type -> createdBy(identifier, type)));
    }

    private static Specification<PdpoLogEntity> createdBy(String identifier, String type) {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("createdByIdentifier"), identifier),
            cb.equal(root.get("createdByIdentifierType"), type));
    }

    private static Specification<PdpoLogEntity> businessIdentifier(String businessIdentifier) {
        return (root, query, cb) -> {
            Join<PdpoLogEntity, PdpoIdentifierEntity> join = root.join("businessIdentifier", JoinType.INNER);
            return cb.equal(join.get("businessIdentifier"), businessIdentifier);
        };
    }

    private static Optional<Specification<PdpoLogEntity>> individualFilterFromCriteria(PdpoLogSearchCriteria criteria) {
        return notBlank(criteria.individualIdentifier())
            .flatMap(individualIdentifier -> notBlank(criteria.individualType())
                .map(individualType -> individualFilterForIdentifierAndType(individualIdentifier, individualType)));
    }

    private static Specification<PdpoLogEntity> individualFilterForIdentifierAndType(
        String individualIdentifier, String individualType) {
        return (root, query, cb) -> {
            query.distinct(true);

            Join<PdpoLogEntity, PdpoLogIndividualEntity> individuals = root.join("individuals", JoinType.INNER);
            return cb.and(
                cb.equal(individuals.get("individualIdentifier"), individualIdentifier),
                cb.equal(individuals.get("individualType"), individualType));
        };
    }

    private static Specification<PdpoLogEntity> createdAfter(LocalDate createdAfter) {
        return (root, query, cb) -> cb.greaterThan(
            root.get("createdAt"),
            createdAfter.atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    private static Specification<PdpoLogEntity> category(PdpoCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

}
