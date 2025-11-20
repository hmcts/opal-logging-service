package uk.gov.hmcts.opal.logging.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;

public interface PdpoLogRepository extends JpaRepository<PdpoLogEntity, Long> {
}
