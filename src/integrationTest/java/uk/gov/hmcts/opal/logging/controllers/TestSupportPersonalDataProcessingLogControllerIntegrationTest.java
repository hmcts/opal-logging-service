package uk.gov.hmcts.opal.logging.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.opal.logging.domain.PdpoCategory;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogResponse;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.generated.dto.SearchPdpoLogRequest;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoIdentifierEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogEntity;
import uk.gov.hmcts.opal.logging.persistence.entity.PdpoLogIndividualEntity;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;
import uk.gov.hmcts.opal.logging.testsupport.AbstractIntegrationTest;

@TestPropertySource(properties = "opal.logging.test-support.enabled=true")
class TestSupportPersonalDataProcessingLogControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PdpoLogRepository logRepository;

    @Autowired
    private PdpoIdentifierRepository identifierRepository;

    @BeforeEach
    void cleanDatabase() {
        logRepository.deleteAll();
        identifierRepository.deleteAll();
    }

    @Test
    void searchByCreatedByReturnsMatchingLogs() throws Exception {
        persistLog("requestor-1", "OPAL_USER_ID", "BRIDGE",
            PdpoCategory.DISCLOSURE, "recipient-1", "EXTERNAL_SERVICE");
        persistLog("requestor-2", "EXTERNAL_SERVICE", "BRIDGE",
            PdpoCategory.COLLECTION, null, null);

        SearchPdpoLogRequest request = new SearchPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("requestor-1").type("OPAL_USER_ID"));

        MvcResult result = mockMvc.perform(post("/test-support/search")
                                               .contentType(APPLICATION_JSON_VALUE)
                                               .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andReturn();

        List<AddPdpoLogResponse> logs = objectMapper.readValue(
            result.getResponse().getContentAsByteArray(),
            new TypeReference<>() {
            });

        assertThat(logs).hasSize(1);
        AddPdpoLogResponse response = logs.getFirst();
        assertThat(response.getCreatedBy().getId()).isEqualTo("requestor-1");
        assertThat(response.getRecipient().getId()).isEqualTo("recipient-1");
        assertThat(response.getIndividuals()).hasSize(1);
    }

    @Test
    void searchByBusinessIdentifierReturnsSortedLogs() throws Exception {
        persistLog("requestor-2", "EXTERNAL_SERVICE", "SHARING",
            PdpoCategory.COLLECTION, null, null,
            OffsetDateTime.parse("2025-11-15T10:00:00Z"));
        persistLog("requestor-1", "OPAL_USER_ID", "SHARING",
            PdpoCategory.COLLECTION, null, null,
            OffsetDateTime.parse("2025-11-15T12:00:00Z"));

        SearchPdpoLogRequest request = new SearchPdpoLogRequest()
            .businessIdentifier("SHARING");

        MvcResult result = mockMvc.perform(post("/test-support/search")
                                               .contentType(APPLICATION_JSON_VALUE)
                                               .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andReturn();

        List<AddPdpoLogResponse> logs = objectMapper.readValue(
            result.getResponse().getContentAsByteArray(),
            new TypeReference<>() {
            });

        assertThat(logs).hasSize(2);
        assertThat(logs.getFirst().getCreatedBy().getId()).isEqualTo("requestor-1");
        assertThat(logs.get(1).getCreatedBy().getId()).isEqualTo("requestor-2");
    }

    @Test
    void searchByCategoryAndBusinessIdentifierReturnsSpecificLogs() throws Exception {
        persistLog("requestor-1", "OPAL_USER_ID", "MATCH",
            PdpoCategory.DISCLOSURE, null, null);
        persistLog("requestor-2", "OPAL_USER_ID", "MATCH",
            PdpoCategory.COLLECTION, null, null);
        persistLog("requestor-3", "OPAL_USER_ID", "OTHER",
            PdpoCategory.DISCLOSURE, null, null);

        SearchPdpoLogRequest request = new SearchPdpoLogRequest()
            .businessIdentifier("MATCH")
            .category(SearchPdpoLogRequest.CategoryEnum.DISCLOSURE);

        MvcResult result = mockMvc.perform(post("/test-support/search")
                                               .contentType(APPLICATION_JSON_VALUE)
                                               .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andReturn();

        List<AddPdpoLogResponse> logs = objectMapper.readValue(
            result.getResponse().getContentAsByteArray(),
            new TypeReference<>() {
            });

        assertThat(logs).hasSize(1);
        AddPdpoLogResponse response = logs.getFirst();
        assertThat(response.getBusinessIdentifier()).isEqualTo("MATCH");
        assertThat(response.getCategory()).isEqualTo(AddPdpoLogResponse.CategoryEnum.DISCLOSURE);
    }

    @Test
    void invalidCreatedByPayloadReturnsBadRequest() throws Exception {
        SearchPdpoLogRequest invalidRequest = new SearchPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("requestor-1"));

        mockMvc.perform(post("/test-support/search")
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsBytes(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void missingAllFiltersReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/test-support/search")
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsBytes(new SearchPdpoLogRequest())))
            .andExpect(status().isBadRequest());
    }

    private PdpoLogEntity persistLog(String createdBy,
                                     String createdByType,
                                     String businessIdentifier,
                                     PdpoCategory category,
                                     String recipientId,
                                     String recipientType) {
        return persistLog(createdBy, createdByType, businessIdentifier, category, recipientId, recipientType,
            OffsetDateTime.parse("2025-11-15T12:00:00Z"));
    }

    private PdpoLogEntity persistLog(String createdBy,
                                     String createdByType,
                                     String businessIdentifier,
                                     PdpoCategory category,
                                     String recipientId,
                                     String recipientType,
                                     OffsetDateTime createdAt) {
        PdpoIdentifierEntity identifier = identifierRepository.findByBusinessIdentifier(businessIdentifier)
            .orElseGet(() -> identifierRepository.save(
                PdpoIdentifierEntity.builder()
                    .businessIdentifier(businessIdentifier)
                    .build()
            ));

        PdpoLogEntity log = PdpoLogEntity.builder()
            .createdByIdentifier(createdBy)
            .createdByIdentifierType(createdByType)
            .createdAt(createdAt)
            .ipAddress("192.168.0.1")
            .category(category)
            .businessIdentifier(identifier)
            .build();

        if (recipientId != null) {
            log.setRecipientIdentifier(recipientId);
            log.setRecipientIdentifierType(recipientType);
        }

        log.addIndividual(PdpoLogIndividualEntity.builder()
            .individualIdentifier("subject-1")
            .individualType("DEFENDANT")
            .build());

        return logRepository.save(log);
    }
}
