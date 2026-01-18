package uk.gov.hmcts.opal.logging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest.CategoryEnum;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.messaging.PdpoQueueMessage;

/**
 * Manual helper that publishes a PDPL message to a queue for developer testing.
 */
@EnabledIfEnvironmentVariable(named = "LOGGING_PDPL_ASB_TEST_ENABLED", matches = "true")
class PdplQueueConnectivityIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdplQueueConnectivityIntegrationTest.class);

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    @Test
    void sendsPdpoMessageToQueue() throws Exception {
        String connectionString = requireEnv("LOGGING_PDPL_CONNECTION_STRING");
        String queueName = requireEnv("LOGGING_PDPL_QUEUE");

        ServiceBusConnectionStringParser.ConnectionDetails details =
            ServiceBusConnectionStringParser.parse(connectionString);

        String remoteUri = "amqps://%s".formatted(details.fullyQualifiedNamespace());
        JmsConnectionFactory connectionFactory = new JmsConnectionFactory(remoteUri);
        connectionFactory.setUsername(details.sharedAccessKeyName());
        connectionFactory.setPassword(details.sharedAccessKey());

        String uniqueMarker = "PDPL-IT-" + UUID.randomUUID();
        AddPdpoLogRequest request = new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("pdpl-it").type("OPAL_USER_ID"))
            .businessIdentifier(uniqueMarker)
            .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
            .ipAddress("10.10.10.10")
            .category(CategoryEnum.COLLECTION)
            .individuals(List.of(new ParticipantIdentifier().id("person-1").type("DEFENDANT")));

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("PDPO", request));

        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue(queueName);
            context.createProducer().send(queue, payload);
        }

        LOGGER.info("Sent PDPL test message with businessIdentifier={}", uniqueMarker);
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set when LOGGING_PDPL_ASB_TEST_ENABLED=true");
        }
        return value;
    }

}
