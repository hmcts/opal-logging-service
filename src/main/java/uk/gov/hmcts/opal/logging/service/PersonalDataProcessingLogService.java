package uk.gov.hmcts.opal.logging.service;

import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import java.util.List;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.generated.dto.SearchPdpoLogRequest;

public interface PersonalDataProcessingLogService {

    PdpoLogEntity recordLog(AddPdpoLogRequest details);

    List<PdpoLogEntity> searchLogs(SearchPdpoLogRequest criteria);
}
