package uk.gov.hmcts.opal.logging.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.logging.generated.dto.SearchPdpoLogRequest;
import uk.gov.hmcts.opal.logging.testsupport.AbstractIntegrationTest;

@TestPropertySource(properties = {
    "opal.logging.test-support.enabled=true",
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1a=false"
})
class PdpoLogSearchRelease1aDisabledIntegrationTest extends AbstractIntegrationTest {

    @Test
    void returnsMethodNotAllowedWhenRelease1aDisabled() throws Exception {
        SearchPdpoLogRequest request = new SearchPdpoLogRequest().businessIdentifier("ACME");

        mockMvc
            .perform(post("/test-support/search").contentType(APPLICATION_JSON_VALUE)
                         .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
