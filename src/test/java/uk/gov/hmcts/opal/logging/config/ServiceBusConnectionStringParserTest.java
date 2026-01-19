package uk.gov.hmcts.opal.logging.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ServiceBusConnectionStringParserTest {

    @Test
    void parsesValidConnectionString() {
        ServiceBusConnectionStringParser.ConnectionDetails details = ServiceBusConnectionStringParser.parse(
            "Endpoint=sb://example.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;"
                + "SharedAccessKey=key;"
                + "EntityPath=logging-pdpl"
        );

        assertThat(details.fullyQualifiedNamespace()).isEqualTo("example.servicebus.windows.net");
        assertThat(details.sharedAccessKeyName()).isEqualTo("RootManageSharedAccessKey");
        assertThat(details.sharedAccessKey()).isEqualTo("key");
    }

    @Test
    void rejectsBlankConnectionString() {
        assertThatThrownBy(() -> ServiceBusConnectionStringParser.parse("  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must not be blank");
    }

    @Test
    void rejectsMissingEndpoint() {
        assertThatThrownBy(() -> ServiceBusConnectionStringParser.parse(
            "SharedAccessKeyName=key-name;SharedAccessKey=key"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missing Endpoint");
    }

    @Test
    void rejectsEndpointWithoutHost() {
        assertThatThrownBy(() -> ServiceBusConnectionStringParser.parse(
            "Endpoint=sb://;SharedAccessKeyName=key-name;SharedAccessKey=key"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Endpoint segment missing host");
    }

    @Test
    void rejectsMissingSharedAccessKeyName() {
        assertThatThrownBy(() -> ServiceBusConnectionStringParser.parse(
            "Endpoint=sb://example.servicebus.windows.net/;SharedAccessKey=key"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missing SharedAccessKeyName");
    }

    @Test
    void rejectsMissingSharedAccessKey() {
        assertThatThrownBy(() -> ServiceBusConnectionStringParser.parse(
            "Endpoint=sb://example.servicebus.windows.net/;SharedAccessKeyName=key-name"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missing SharedAccessKey");
    }
}
