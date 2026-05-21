package uk.gov.hmcts.opal.logging.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.logging.testsupport.TestHttpClient;

@Tag("R1AOff")
@Tag("JIRA-STORY:PO-3764")
@Tag("JIRA-EPIC:PO-3685")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "opal.logging.test-support.enabled=true",
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1a=false"
})
class PersonalDataProcessingLogRelease1aDisabledFunctionalTest extends AbstractRelease1aFeatureToggleFunctionalTest {

    /**
     * Verifies the add PDPO log endpoint is blocked when the release-1a fallback flag is disabled.
     *
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    @Test
    void addPdpoLogIsBlockedWhenRelease1aFallbackIsDisabled() throws IOException, GeneralSecurityException {
        TestHttpClient.Response response = addPdpoLog();

        assertFeatureDisabled(response);
    }

    /**
     * Verifies the search logs endpoint is blocked when the release-1a fallback flag is disabled.
     *
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    @Test
    void searchLogsIsBlockedWhenRelease1aFallbackIsDisabled() throws IOException, GeneralSecurityException {
        TestHttpClient.Response response = searchLogs();

        assertFeatureDisabled(response);
    }
}
