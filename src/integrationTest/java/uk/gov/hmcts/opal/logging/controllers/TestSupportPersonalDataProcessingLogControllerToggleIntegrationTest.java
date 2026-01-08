package uk.gov.hmcts.opal.logging.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.logging.generated.dto.SearchPdpoLogRequest;
import uk.gov.hmcts.opal.logging.testsupport.AbstractIntegrationTest;

@TestPropertySource(properties = "opal.logging.test-support.enabled=false")
class TestSupportPersonalDataProcessingLogControllerToggleIntegrationTest extends AbstractIntegrationTest {

    @Test
    void endpointReturnsNotFoundWhenToggleDisabled() throws Exception {
        mockMvc.perform(post("/test-support/search")
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsBytes(new SearchPdpoLogRequest()
                                                                     .businessIdentifier("ACME"))))
            .andExpect(status().isNotFound());
    }
}
