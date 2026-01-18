package uk.gov.hmcts.opal.logging.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest.CategoryEnum;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;
import uk.gov.hmcts.opal.logging.testsupport.AbstractIntegrationTest;

class PdpoQueueConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PdpoQueueConsumer consumer;
    @Autowired
    private PdpoIdentifierRepository identifierRepository;
    @Autowired
    private PdpoLogRepository logRepository;

    @BeforeEach
    void clean() {
        logRepository.deleteAll();
        identifierRepository.deleteAll();
    }

    @Test
    @Transactional
    void consumesMessageAndPersistsLog() throws Exception {
        AddPdpoLogRequest request = new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("queue-user").type("OPAL_USER_ID"))
            .businessIdentifier("QueueCo")
            .createdAt(OffsetDateTime.parse("2025-11-19T14:05:00Z"))
            .ipAddress("10.10.10.10")
            .category(CategoryEnum.COLLECTION)
            .individuals(List.of(new ParticipantIdentifier().id("person-1").type("DEFENDANT")));

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("PDPO", request));

        consumer.consume(payload);

        assertThat(identifierRepository.count()).isEqualTo(1);
        assertThat(logRepository.count()).isEqualTo(1);

        PdpoLogEntity persisted = logRepository.findAll().get(0);
        assertThat(persisted.getCreatedByIdentifier()).isEqualTo("queue-user");
        assertThat(persisted.getCreatedByIdentifierType()).isEqualTo("OPAL_USER_ID");
        assertThat(persisted.getBusinessIdentifier().getBusinessIdentifier()).isEqualTo("QueueCo");
        assertThat(persisted.getCategory()).isEqualTo(PdpoCategory.COLLECTION);
    }
}
