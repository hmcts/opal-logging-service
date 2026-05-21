package uk.gov.hmcts.opal.logging.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogResponse;

@Tag("R1AOn")
@Tag("JIRA-STORY:PO-3764")
@Tag("JIRA-EPIC:PO-3685")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "opal.logging.test-support.enabled=true",
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1a=true"
})
class PersonalDataProcessingLogRelease1aEnabledFunctionalTest extends AbstractRelease1aFeatureToggleFunctionalTest {

    /**
     * Verifies the add PDPO log endpoint remains accessible when the release-1a fallback flag is enabled.
     *
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    @Test
    void addPdpoLogIsAccessibleWhenRelease1aFallbackIsEnabled() throws IOException, GeneralSecurityException {
        String businessIdentifier = uniqueBusinessIdentifier();

        AddPdpoLogResponse response = createPdpoLog(businessIdentifier);

        assertThat(response.getBusinessIdentifier()).isEqualTo(businessIdentifier);
        assertThat(response.getCreatedBy().getId()).isEqualTo("requestor-1");
    }

    /**
     * Verifies the search logs endpoint remains accessible when the release-1a fallback flag is enabled.
     *
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    @Test
    void searchLogsIsAccessibleWhenRelease1aFallbackIsEnabled() throws IOException, GeneralSecurityException {
        String businessIdentifier = uniqueBusinessIdentifier();
        createPdpoLog(businessIdentifier);

        List<AddPdpoLogResponse> response = searchLogs(businessIdentifier);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getBusinessIdentifier()).isEqualTo(businessIdentifier);
        assertThat(response.getFirst().getCreatedBy().getId()).isEqualTo("requestor-1");
    }
}
