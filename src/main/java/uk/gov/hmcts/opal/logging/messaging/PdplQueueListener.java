package uk.gov.hmcts.opal.logging.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.PdplQueueListener")
@ConditionalOnProperty(prefix = "opal.logging.pdpl.consumer", name = "enabled", havingValue = "true")
public class PdplQueueListener {

    private final PdpoQueueConsumer consumer;

    @JmsListener(
        destination = "${opal.logging.pdpl.consumer.queue-name}",
        containerFactory = "pdplListenerContainerFactory"
    )
    public void onMessage(String messagePayload) {
        log.debug(":QUEUE:pdpl:received");
        consumer.consume(messagePayload);
    }
}
