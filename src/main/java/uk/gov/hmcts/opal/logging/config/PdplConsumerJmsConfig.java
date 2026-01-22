package uk.gov.hmcts.opal.logging.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

@EnableJms
@Configuration
@EnableConfigurationProperties(PdplConsumerProperties.class)
@ConditionalOnProperty(prefix = "opal.logging.pdpl.consumer", name = "enabled", havingValue = "true")
public class PdplConsumerJmsConfig {

    @Bean
    public ConnectionFactory pdplConsumerConnectionFactory(PdplConsumerProperties properties) {
        ServiceBusConnectionStringParser.ConnectionDetails details =
            ServiceBusConnectionStringParser.parse(properties.getConnectionString());

        String remoteUri = "%s://%s?amqp.idleTimeout=%d".formatted(
            properties.getProtocol(),
            details.fullyQualifiedNamespace(),
            properties.getIdleTimeoutMs()
        );

        JmsConnectionFactory qpidFactory = new JmsConnectionFactory(remoteUri);
        qpidFactory.setUsername(details.sharedAccessKeyName());
        qpidFactory.setPassword(details.sharedAccessKey());

        return new CachingConnectionFactory(qpidFactory);
    }

    @Bean
    public DefaultJmsListenerContainerFactory pdplListenerContainerFactory(
        ConnectionFactory pdplConsumerConnectionFactory
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(pdplConsumerConnectionFactory);
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return factory;
    }
}
