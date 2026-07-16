package uk.gov.hmcts.opal.logging.messaging;

public record PdpoQueueMessage(String logType, PdpoQueueDetails details) {
}
