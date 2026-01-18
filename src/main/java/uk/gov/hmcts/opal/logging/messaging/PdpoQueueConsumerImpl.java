package uk.gov.hmcts.opal.logging.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.service.PersonalDataProcessingLogService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.PdpoQueueConsumer")
public class PdpoQueueConsumerImpl implements PdpoQueueConsumer {

    static final String PDPO_LOG_TYPE = "PDPO";

    private final ObjectMapper objectMapper;
    private final PersonalDataProcessingLogService logService;

    @Override
    public void consume(String messagePayload) {
        PdpoQueueMessage message = parse(messagePayload);
        String logType = message.logType();
        if (!PDPO_LOG_TYPE.equals(logType)) {
            throw new IllegalArgumentException("Unsupported log type: " + logType);
        }
        AddPdpoLogRequest details = message.details();
        if (details == null) {
            throw new IllegalArgumentException("PDPO message payload missing details");
        }
        logService.recordLog(details);
        log.info(":QUEUE:pdpl:processed businessIdentifier={} category={}",
            details.getBusinessIdentifier(),
            details.getCategory());
    }

    private PdpoQueueMessage parse(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            throw new IllegalArgumentException("PDPO message payload is blank");
        }
        try {
            return objectMapper.readValue(messagePayload, PdpoQueueMessage.class);
        } catch (IOException ex) {
            log.warn(":QUEUE:pdpl:parse-failed", ex);
            throw new IllegalArgumentException("Unable to parse PDPO message payload", ex);
        }
    }
}
