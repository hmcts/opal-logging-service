package uk.gov.hmcts.opal.logging.persistence.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.Optional;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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
            notBlank(criteria.individualIdentifier()).map(PdpoLogSpecifications::individualIdentifier),
            notNull(criteria.category()).map(PdpoLogSpecifications::category)
        ));
    }

    private Optional<Specification<PdpoLogEntity>> createdByFilter(PdpoLogSearchCriteria criteria) {
        return notBlank(criteria.createdByIdentifier())
            .flatMap(identifier -> notBlank(criteria.createdByType())
                .map(type -> createdBy(identifier, type)));
    }

    private static Specification<PdpoLogEntity> createdBy(String identifier, String type) {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("createdByIdentifier"), identifier),
            cb.equal(root.get("createdByIdentifierType"), type)
        );
    }

    private static Specification<PdpoLogEntity> businessIdentifier(String businessIdentifier) {
        return (root, query, cb) -> {
            Join<PdpoLogEntity, PdpoIdentifierEntity> join = root.join("businessIdentifier", JoinType.INNER);
            return cb.equal(join.get("businessIdentifier"), businessIdentifier);
        };
    }

    private static Specification<PdpoLogEntity> individualIdentifier(String individualId) {
        return (root, query, cb) -> {
            query.distinct(true);

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<PdpoLogEntity> correlatedRoot = subquery.correlate(root);
            Join<PdpoLogEntity, PdpoLogIndividualEntity> individuals =
                correlatedRoot.join("individuals", JoinType.INNER);

            subquery.select(correlatedRoot.get("id"))
                .where(cb.equal(individuals.get("individualIdentifier"), individualId));

            return cb.exists(subquery);
        };
    }

    private static Specification<PdpoLogEntity> category(PdpoCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

}
