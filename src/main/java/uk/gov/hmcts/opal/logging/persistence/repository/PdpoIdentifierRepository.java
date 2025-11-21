package uk.gov.hmcts.opal.logging.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;

public interface PdpoIdentifierRepository extends JpaRepository<PdpoIdentifierEntity, Long> {

    Optional<PdpoIdentifierEntity> findByBusinessIdentifier(String businessIdentifier);
}
