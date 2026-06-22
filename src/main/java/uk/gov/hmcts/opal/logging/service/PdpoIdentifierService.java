package uk.gov.hmcts.opal.logging.service;

import java.util.Optional;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;

public interface PdpoIdentifierService {

    PdpoIdentifierEntity findOrCreate(String businessIdentifier);

    Optional<PdpoIdentifierEntity> findByBusinessIdentifier(String businessIdentifier);
}
