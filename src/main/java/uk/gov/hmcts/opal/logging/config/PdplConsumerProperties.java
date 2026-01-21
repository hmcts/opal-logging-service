package uk.gov.hmcts.opal.logging.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opal.logging.pdpl.consumer")
@Data
public class PdplConsumerProperties {

    private boolean enabled;
    private String connectionString;
    private String queueName;
    private String protocol = "amqps";

}
