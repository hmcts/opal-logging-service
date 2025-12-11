package uk.gov.hmcts.opal.logging.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.generated.model.AddPDPLRequestPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.generated.model.AddPDPLRequestPersonalDataProcessingLogging.CategoryEnum;
import uk.gov.hmcts.opal.generated.model.PDPLIdentifierPersonalDataProcessingLogging;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoIdentifierRepository;
import uk.gov.hmcts.opal.logging.persistence.repository.PdpoLogRepository;
import uk.gov.hmcts.opal.logging.testsupport.AbstractIntegrationTest;

@TestPropertySource(properties = "opal.logging.test-support.enabled=true")
class PersonalDataProcessingLogControllerIntegrationTest extends AbstractIntegrationTest {

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
    void validRequestReturnsCreatedAndPersistsLog() throws Exception {
        mockMvc.perform(post("/log/pdpo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(baseRequest())))
            .andExpect(status().isCreated());

        assertThat(logRepository.count()).isEqualTo(1);
    }

    @Test
    void invalidPayloadReturnsBadRequestAndDoesNotPersist() throws Exception {
        AddPDPLRequestPersonalDataProcessingLogging invalidRequest = baseRequest()
            .businessIdentifier(null);

        mockMvc.perform(post("/log/pdpo")
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsBytes(invalidRequest)))
            .andExpect(status().isBadRequest());

        assertThat(logRepository.count()).isZero();
    }

    private AddPDPLRequestPersonalDataProcessingLogging baseRequest() {
        return new AddPDPLRequestPersonalDataProcessingLogging()
            .createdBy(new PDPLIdentifierPersonalDataProcessingLogging().id("requestor-1").type("OPAL_USER_ID"))
            .businessIdentifier("SharingCo")
            .createdAt(OffsetDateTime.parse("2025-11-15T12:45:00Z"))
            .ipAddress("192.168.1.10")
            .category(CategoryEnum.DISCLOSURE)
            .recipient(new PDPLIdentifierPersonalDataProcessingLogging().id("recipient-42").type("EXTERNAL_SERVICE"))
            .addIndividualsItem(new PDPLIdentifierPersonalDataProcessingLogging().id("person-1").type("DEFENDANT"));
    }
}
