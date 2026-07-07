package uk.gov.hmcts.opal.logging.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest.CategoryEnum;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.service.PersonalDataProcessingLogService;

class PdpoQueueConsumerImplTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();
    private final PdpoQueueMessageMapper queueMessageMapper = Mappers.getMapper(PdpoQueueMessageMapper.class);

    @Test
    void consumeParsesMessageAndPersistsLog() throws Exception {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, queueMessageMapper, logService);

        AddPdpoLogRequest details = new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("user-1").type("OPAL_USER_ID"))
            .createdAt(OffsetDateTime.parse("2025-11-09T10:15:30Z"))
            .businessIdentifier("ACME")
            .ipAddress("10.0.0.1")
            .category(CategoryEnum.COLLECTION)
            .individuals(List.of(new ParticipantIdentifier().id("ind-1").type("DEFENDANT")));

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("PDPO", queueDetails(details)));

        consumer.consume(payload);

        ArgumentCaptor<AddPdpoLogRequest> captor = ArgumentCaptor.forClass(AddPdpoLogRequest.class);
        verify(logService).recordLog(captor.capture());
        AddPdpoLogRequest captured = captor.getValue();
        assertThat(captured).isEqualTo(details);
    }

    @Test
    void consumeRejectsUnsupportedLogType() throws Exception {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, queueMessageMapper, logService);

        AddPdpoLogRequest details = new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("user-1").type("OPAL_USER_ID"))
            .createdAt(OffsetDateTime.parse("2025-11-09T10:15:30Z"))
            .businessIdentifier("ACME")
            .ipAddress("10.0.0.1")
            .category(CategoryEnum.COLLECTION)
            .individuals(List.of(new ParticipantIdentifier().id("ind-1").type("DEFENDANT")));

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("OTHER", queueDetails(details)));

        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported log type");

        verify(logService, never()).recordLog(Mockito.any());
    }

    @Test
    void consumeRejectsBlankPayload() {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, queueMessageMapper, logService);

        assertThatThrownBy(() -> consumer.consume("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payload is blank");

        verify(logService, never()).recordLog(Mockito.any());
    }

    @Test
    void consumeRejectsInvalidJson() {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, queueMessageMapper, logService);

        assertThatThrownBy(() -> consumer.consume("{invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse");

        verify(logService, never()).recordLog(Mockito.any());
    }

    @Test
    void consumeRejectsMissingDetails() throws Exception {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, queueMessageMapper, logService);

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("PDPO", null));

        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missing details");

        verify(logService, never()).recordLog(Mockito.any());
    }

    @Test
    void consumeParsesCompactIndividualsAndPersistsLog() {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, queueMessageMapper, logService);

        String payload = """
            {
              "log_type": "PDPO",
              "details": {
                "created_by": {
                  "id": "user-1",
                  "type": "OPAL_USER_ID"
                },
                "created_at": "2025-11-09T10:15:30Z",
                "business_identifier": "ACME",
                "ip_address": "10.0.0.1",
                "category": "Collection",
                "individuals": {
                  "DEFENDANT": ["ind-1", "ind-2"],
                  "MINOR_CREDITOR": ["ind-3"]
                }
              }
            }
            """;

        consumer.consume(payload);

        ArgumentCaptor<AddPdpoLogRequest> captor = ArgumentCaptor.forClass(AddPdpoLogRequest.class);
        verify(logService).recordLog(captor.capture());
        AddPdpoLogRequest captured = captor.getValue();
        assertThat(captured.getIndividuals()).containsExactly(
            new ParticipantIdentifier().id("ind-1").type("DEFENDANT"),
            new ParticipantIdentifier().id("ind-2").type("DEFENDANT"),
            new ParticipantIdentifier().id("ind-3").type("MINOR_CREDITOR")
        );
        assertThat(captured.getBusinessIdentifier()).isEqualTo("ACME");
    }

    @Test
    void consumeRejectsInvalidCompactIndividuals() {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, queueMessageMapper, logService);

        String payload = """
            {
              "log_type": "PDPO",
              "details": {
                "created_by": {
                  "id": "user-1",
                  "type": "OPAL_USER_ID"
                },
                "created_at": "2025-11-09T10:15:30Z",
                "business_identifier": "ACME",
                "ip_address": "10.0.0.1",
                "category": "Collection",
                "individuals": {
                  "DEFENDANT": "ind-1"
                }
              }
            }
            """;

        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse");

        verify(logService, never()).recordLog(Mockito.any());
    }

    private PdpoQueueDetails queueDetails(AddPdpoLogRequest details) {
        PdpoQueueDetails queueDetails = new PdpoQueueDetails();
        queueDetails.setCreatedBy(details.getCreatedBy());
        queueDetails.setCreatedAt(details.getCreatedAt());
        queueDetails.setBusinessIdentifier(details.getBusinessIdentifier());
        queueDetails.setIpAddress(details.getIpAddress());
        queueDetails.setCategory(details.getCategory());
        queueDetails.setRecipient(details.getRecipient());
        queueDetails.setIndividuals(objectMapper.valueToTree(details.getIndividuals()));
        return queueDetails;
    }
}
