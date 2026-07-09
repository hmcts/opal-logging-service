package uk.gov.hmcts.opal.logging.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

class PdplConsumerJmsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(PdplConsumerJmsConfig.class);

    @Test
    void loadsJmsBeansWhenEnabled() {
        contextRunner
            .withPropertyValues(
                "opal.logging.pdpl.consumer.enabled=true",
                "opal.logging.pdpl.consumer.connection-string=Endpoint=sb://example.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key",
                "opal.logging.pdpl.consumer.queue-name=logging-pdpl")
            .run(context -> {
                assertThat(context).hasSingleBean(PdplConsumerProperties.class);
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactory.class);
                DefaultJmsListenerContainerFactory factory =
                    context.getBean(DefaultJmsListenerContainerFactory.class);
                DirectFieldAccessor accessor = new DirectFieldAccessor(factory);
                assertThat(accessor.getPropertyValue("sessionAcknowledgeMode"))
                    .isEqualTo(Session.CLIENT_ACKNOWLEDGE);
            });
    }

    @Test
    void skipsJmsBeansWhenDisabled() {
        contextRunner
            .withPropertyValues("opal.logging.pdpl.consumer.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(ConnectionFactory.class));
    }
}
