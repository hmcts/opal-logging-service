package uk.gov.hmcts.opal.logging.service;

import uk.gov.hmcts.opal.logging.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;

public interface PersonalDataProcessingLogService {

    PdpoLogEntity recordLog(PersonalDataProcessingLogDetails details);
}
