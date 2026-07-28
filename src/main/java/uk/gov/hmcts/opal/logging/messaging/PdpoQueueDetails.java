package uk.gov.hmcts.opal.logging.messaging;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;

@Getter
@Setter
public class PdpoQueueDetails {

    private ParticipantIdentifier createdBy;
    private String businessIdentifier;
    private OffsetDateTime createdAt;
    private String ipAddress;
    private AddPdpoLogRequest.CategoryEnum category;
    private ParticipantIdentifier recipient;
    private JsonNode individuals;

}
