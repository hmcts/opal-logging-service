package uk.gov.hmcts.opal.logging.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;

public record PdpoQueueMessage(
    @JsonProperty("log_type") String logType,
    @JsonProperty("details") AddPdpoLogRequest details
) {
}
