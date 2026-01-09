package uk.gov.hmcts.opal.logging.persistence.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;

@Testcontainers
@DataJpaTest
@Import(PdpoLogSpecifications.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PdpoLogSpecificationsTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.9")
        .withDatabaseName("opal-logging-spec-test")
        .withUsername("opal-logging")
        .withPassword("opal-logging");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private PdpoLogRepository logRepository;

    @Autowired
    private PdpoIdentifierRepository identifierRepository;

    @Autowired
    private PdpoLogSpecifications specifications;

    @BeforeEach
    void cleanDb() {
        logRepository.deleteAll();
        identifierRepository.deleteAll();
    }

    @Test
    void findBySearchCriteriaFiltersByCreatedByPair() {
        persistLog("user-1", "OPAL_USER_ID", "ACC-1", PdpoCategory.DISCLOSURE);
        persistLog("user-2", "EXTERNAL_SERVICE", "ACC-1", PdpoCategory.DISCLOSURE);

        PdpoLogSearchCriteria criteria = new PdpoLogSearchCriteria("user-1", "OPAL_USER_ID", null, null);

        List<PdpoLogEntity> results = logRepository.findAll(
            specifications.findBySearchCriteria(criteria),
            Sort.by("id")
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCreatedByIdentifier()).isEqualTo("user-1");
    }

    @Test
    void findBySearchCriteriaFiltersByBusinessAndCategory() {
        persistLog("user-1", "OPAL_USER_ID", "ACC-1", PdpoCategory.DISCLOSURE);
        persistLog("user-2", "OPAL_USER_ID", "ACC-1", PdpoCategory.COLLECTION);
        persistLog("user-3", "OPAL_USER_ID", "ACC-2", PdpoCategory.DISCLOSURE);

        PdpoLogSearchCriteria criteria = new PdpoLogSearchCriteria(
            null,
            null,
            "ACC-1",
            PdpoCategory.DISCLOSURE
        );

        List<PdpoLogEntity> results = logRepository.findAll(
            specifications.findBySearchCriteria(criteria),
            Sort.by("id")
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getBusinessIdentifier().getBusinessIdentifier()).isEqualTo("ACC-1");
        assertThat(results.getFirst().getCategory()).isEqualTo(PdpoCategory.DISCLOSURE);
    }

    private void persistLog(String createdBy,
                            String createdByType,
                            String businessIdentifier,
                            PdpoCategory category) {
        PdpoIdentifierEntity identifier = identifierRepository.findByBusinessIdentifier(businessIdentifier)
            .orElseGet(() -> identifierRepository.save(
                PdpoIdentifierEntity.builder()
                    .businessIdentifier(businessIdentifier)
                    .build()
            ));

        PdpoLogEntity entity = PdpoLogEntity.builder()
            .createdByIdentifier(createdBy)
            .createdByIdentifierType(createdByType)
            .businessIdentifier(identifier)
            .category(category)
            .createdAt(OffsetDateTime.parse("2024-05-01T10:00:00Z"))
            .ipAddress("127.0.0.1")
            .build();

        logRepository.save(entity);
    }
}
