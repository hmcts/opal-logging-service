package uk.gov.hmcts.opal.logging.service;

import uk.gov.hmcts.opal.generated.model.AddPDPLRequestPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;

public interface PersonalDataProcessingLogService {

    PdpoLogEntity recordLog(AddPDPLRequestPersonalDataProcessingLogging details);
}
