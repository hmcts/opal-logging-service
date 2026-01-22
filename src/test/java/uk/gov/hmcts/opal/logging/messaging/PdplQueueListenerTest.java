package uk.gov.hmcts.opal.logging.messaging;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PdplQueueListenerTest {

    @Test
    void delegatesMessageToConsumer() {
        PdpoQueueConsumer consumer = Mockito.mock(PdpoQueueConsumer.class);
        PdplQueueListener listener = new PdplQueueListener(consumer);

        listener.onMessage("{\"log_type\":\"PDPO\"}");

        verify(consumer).consume("{\"log_type\":\"PDPO\"}");
    }
}
