package uk.gov.hmcts.opal.logging.service;

import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;

public interface PersonalDataProcessingLogService {

    PdpoLogEntity recordLog(AddPdpoLogRequest details);
}
