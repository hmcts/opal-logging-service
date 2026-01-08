package uk.gov.hmcts.opal.logging.persistence.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;

/**
 * Factory for {@link Specification} instances used to query {@link PdpoLogEntity}.
 */
@Component
public class PdpoLogSpecifications extends EntitySpecs<PdpoLogEntity> {

    public Specification<PdpoLogEntity> findBySearchCriteria(PdpoLogSearchCriteria criteria) {
        return Specification.allOf(specificationList(
            createdByFilter(criteria),
            notBlank(criteria.businessIdentifier()).map(PdpoLogSpecifications::businessIdentifier),
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

    private static Specification<PdpoLogEntity> category(PdpoCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

}
