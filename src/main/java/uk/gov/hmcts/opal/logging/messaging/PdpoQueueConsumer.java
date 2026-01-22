package uk.gov.hmcts.opal.logging.messaging;

/**
 * Handles PDPO queue messages once they are received from Azure Service Bus.
 */
public interface PdpoQueueConsumer {

    void consume(String messagePayload);
}
