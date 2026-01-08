package uk.gov.hmcts.opal.logging.persistence.repository;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;

public interface PdpoLogRepository extends JpaRepository<PdpoLogEntity, Long>,
    JpaSpecificationExecutor<PdpoLogEntity> {

    @Override
    @EntityGraph(attributePaths = {"businessIdentifier", "individuals"})
    List<PdpoLogEntity> findAll(Specification<PdpoLogEntity> spec, Sort sort);
}
