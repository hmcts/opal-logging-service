package uk.gov.hmcts.opal.logging.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
    void consumesLegacyMessageAndPersistsLog() throws Exception {
        AddPdpoLogRequest request = new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("queue-user").type("OPAL_USER_ID"))
            .businessIdentifier("QueueCo")
            .createdAt(OffsetDateTime.parse("2025-11-19T14:05:00Z"))
            .ipAddress("10.10.10.10")
            .category(CategoryEnum.COLLECTION)
            .individuals(List.of(new ParticipantIdentifier().id("person-1").type("DEFENDANT")));

        String payload = objectMapper.writeValueAsString(new PdpoQueueMessage("PDPO", queueDetails(request)));

        consumer.consume(payload);

        assertThat(identifierRepository.count()).isEqualTo(1);
        assertThat(logRepository.count()).isEqualTo(1);

        PdpoLogEntity persisted = logRepository.findAll().get(0);
        assertThat(persisted.getCreatedByIdentifier()).isEqualTo("queue-user");
        assertThat(persisted.getCreatedByIdentifierType()).isEqualTo("OPAL_USER_ID");
        assertThat(persisted.getBusinessIdentifier().getBusinessIdentifier()).isEqualTo("QueueCo");
        assertThat(persisted.getCategory()).isEqualTo(PdpoCategory.COLLECTION);
    }

    @Test
    @Transactional
    void consumesCompactMessageAndPersistsLog() throws Exception {
        consumer.consume(compactPayload());

        PdpoLogEntity persisted = logRepository.findAll().getFirst();
        assertThat(persisted.getBusinessIdentifier().getBusinessIdentifier()).isEqualTo("QueueCompact");
        assertThat(persisted.getIndividuals())
            .extracting("individualType", "individualIdentifier")
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple("DEFENDANT", "person-1"),
                org.assertj.core.groups.Tuple.tuple("DEFENDANT", "person-2"),
                org.assertj.core.groups.Tuple.tuple("MINOR_CREDITOR", "person-3")
            );
    }

    private PdpoQueueDetails queueDetails(AddPdpoLogRequest request) {
        PdpoQueueDetails queueDetails = new PdpoQueueDetails();
        queueDetails.setCreatedBy(request.getCreatedBy());
        queueDetails.setBusinessIdentifier(request.getBusinessIdentifier());
        queueDetails.setCreatedAt(request.getCreatedAt());
        queueDetails.setIpAddress(request.getIpAddress());
        queueDetails.setCategory(request.getCategory());
        queueDetails.setRecipient(request.getRecipient());
        queueDetails.setIndividuals(objectMapper.valueToTree(request.getIndividuals()));
        return queueDetails;
    }

    private String compactPayload() {
        return objectMapper.writeValueAsString(Map.of(
            "log_type", "PDPO",
            "details", Map.of(
                "created_by", Map.of("id", "queue-user", "type", "OPAL_USER_ID"),
                "business_identifier", "QueueCompact",
                "created_at", "2025-11-19T14:05:00Z",
                "ip_address", "10.10.10.10",
                "category", "Collection",
                "individuals", Map.of(
                    "DEFENDANT", List.of("person-1", "person-2"),
                    "MINOR_CREDITOR", List.of("person-3")))));
    }
}
