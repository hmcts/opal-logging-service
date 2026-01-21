package uk.gov.hmcts.opal.logging.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest.CategoryEnum;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.service.PersonalDataProcessingLogService;

class PdpoQueueConsumerImplTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();

    @Test
    void consumeParsesMessageAndPersistsLog() throws Exception {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, logService);

        AddPdpoLogRequest details = new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("user-1").type("OPAL_USER_ID"))
            .createdAt(OffsetDateTime.parse("2025-11-09T10:15:30Z"))
            .businessIdentifier("ACME")
            .ipAddress("10.0.0.1")
            .category(CategoryEnum.COLLECTION)
            .individuals(List.of(new ParticipantIdentifier().id("ind-1").type("DEFENDANT")));

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("PDPO", details));

        consumer.consume(payload);

        ArgumentCaptor<AddPdpoLogRequest> captor = ArgumentCaptor.forClass(AddPdpoLogRequest.class);
        verify(logService).recordLog(captor.capture());
        AddPdpoLogRequest captured = captor.getValue();
        assertThat(captured.getBusinessIdentifier()).isEqualTo("ACME");
        assertThat(captured.getCreatedBy().getId()).isEqualTo("user-1");
        assertThat(captured.getIndividuals()).hasSize(1);
    }

    @Test
    void consumeRejectsUnsupportedLogType() throws Exception {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, logService);

        AddPdpoLogRequest details = new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("user-1").type("OPAL_USER_ID"))
            .createdAt(OffsetDateTime.parse("2025-11-09T10:15:30Z"))
            .businessIdentifier("ACME")
            .ipAddress("10.0.0.1")
            .category(CategoryEnum.COLLECTION)
            .individuals(List.of(new ParticipantIdentifier().id("ind-1").type("DEFENDANT")));

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("OTHER", details));

        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported log type");

        verify(logService, never()).recordLog(Mockito.any());
    }

    @Test
    void consumeRejectsBlankPayload() {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, logService);

        assertThatThrownBy(() -> consumer.consume("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payload is blank");

        verify(logService, never()).recordLog(Mockito.any());
    }

    @Test
    void consumeRejectsInvalidJson() {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, logService);

        assertThatThrownBy(() -> consumer.consume("{invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse");

        verify(logService, never()).recordLog(Mockito.any());
    }

    @Test
    void consumeRejectsMissingDetails() throws Exception {
        PersonalDataProcessingLogService logService = Mockito.mock(PersonalDataProcessingLogService.class);
        PdpoQueueConsumerImpl consumer = new PdpoQueueConsumerImpl(objectMapper, logService);

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("PDPO", null));

        assertThatThrownBy(() -> consumer.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missing details");

        verify(logService, never()).recordLog(Mockito.any());
    }
}
