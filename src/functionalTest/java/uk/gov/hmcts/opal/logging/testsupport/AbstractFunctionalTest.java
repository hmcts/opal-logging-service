package uk.gov.hmcts.opal.logging.testsupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.ObjectMapper;

import static uk.gov.hmcts.opal.logging.testsupport.TestContainerConfig.POSTGRES_CONTAINER;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
@ContextConfiguration(classes = {TestContainerConfig.class})
@AutoConfigureMockMvc
public class AbstractFunctionalTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${TEST_URL:}")
    private String configuredTestUrl;

    @LocalServerPort
    private int localServerPort;

    /**
     * Registers container-backed datasource and Flyway properties for the functional test context.
     *
     * @param registry Spring registry used to publish dynamic properties
     */
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.flyway.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.flyway.password", POSTGRES_CONTAINER::getPassword);
    }

    /**
     * Resolves the base URL for HTTP-level functional tests.
     *
     * @return configured test URL when provided, otherwise the random local server URL
     */
    protected String testUrl() {
        if (configuredTestUrl != null && !configuredTestUrl.isBlank()) {
            return configuredTestUrl;
        }

        return "http://localhost:" + localServerPort;
    }
}
