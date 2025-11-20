package uk.gov.hmcts.opal.logging.domain;

import uk.gov.hmcts.opal.logging.dto.PersonalDataProcessingCategory;

/**
 * Internal representation of the PDPO categories persisted in the database.
 */
public enum PdpoCategory {
    COLLECTION,
    ALTERATION,
    CONSULTATION,
    DISCLOSURE,
    COMBINATION,
    ERASURE;

    public static PdpoCategory from(PersonalDataProcessingCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("category must be provided");
        }
        return switch (category) {
            case COLLECTION -> COLLECTION;
            case ALTERATION -> ALTERATION;
            case CONSULTATION -> CONSULTATION;
            case DISCLOSURE_TRANSFERS -> DISCLOSURE;
            case COMBINATION -> COMBINATION;
            case ERASURE -> ERASURE;
        };
    }

    public boolean requiresRecipient() {
        return this == DISCLOSURE;
    }
}
