package uk.gov.hmcts.opal.logging.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest.CategoryEnum;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;
import uk.gov.hmcts.opal.logging.testsupport.AbstractIntegrationTest;

@TestPropertySource(properties = "opal.logging.test-support.enabled=false")
class PersonalDataProcessingLogControllerToggleIntegrationTest extends AbstractIntegrationTest {

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
    void endpointReturnsNotFoundWhenToggleDisabled() throws Exception {
        mockMvc.perform(post("/log/pdpo")
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsBytes(baseRequest())))
            .andExpect(status().isNotFound());

        assertThat(logRepository.count()).isZero();
    }

    private AddPdpoLogRequest baseRequest() {
        return new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("requestor-1").type("OPAL_USER_ID"))
            .businessIdentifier("SharingCo")
            .createdAt(OffsetDateTime.parse("2025-11-15T12:45:00Z"))
            .ipAddress("192.168.1.10")
            .category(CategoryEnum.COLLECTION)
            .addIndividualsItem(new ParticipantIdentifier().id("person-1").type("DEFENDANT"));
    }
}
