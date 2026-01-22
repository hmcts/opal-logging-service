package uk.gov.hmcts.opal.logging.messaging;

import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;

public record PdpoQueueMessage(
    String logType,
    AddPdpoLogRequest details
) {
}
