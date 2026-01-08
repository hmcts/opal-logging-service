package uk.gov.hmcts.opal.logging.persistence.specification;

import uk.gov.hmcts.opal.logging.domain.PdpoCategory;

/**
 * Normalised filters supplied to {@link PdpoLogSpecifications}.
 */
public record PdpoLogSearchCriteria(String createdByIdentifier,
                                    String createdByType,
                                    String businessIdentifier,
                                    PdpoCategory category) {
}
