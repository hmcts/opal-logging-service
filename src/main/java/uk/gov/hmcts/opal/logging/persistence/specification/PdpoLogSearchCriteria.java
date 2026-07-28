package uk.gov.hmcts.opal.logging.persistence.specification;

import java.time.LocalDate;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;

/**
 * Normalised filters supplied to {@link PdpoLogSpecifications}.
 */
public record PdpoLogSearchCriteria(String createdByIdentifier,
                                    String createdByType, String businessIdentifier, String individualIdentifier,
                                    String individualType, PdpoCategory category, LocalDate createdAfter) {

}
