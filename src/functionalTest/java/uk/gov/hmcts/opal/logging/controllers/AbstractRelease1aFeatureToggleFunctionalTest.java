package uk.gov.hmcts.opal.logging.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogRequest.CategoryEnum;
import uk.gov.hmcts.opal.logging.generated.dto.AddPdpoLogResponse;
import uk.gov.hmcts.opal.logging.generated.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.generated.dto.SearchPdpoLogRequest;
import uk.gov.hmcts.opal.logging.testsupport.AbstractFunctionalTest;
import uk.gov.hmcts.opal.logging.testsupport.TestHttpClient;
import tools.jackson.core.type.TypeReference;

abstract class AbstractRelease1aFeatureToggleFunctionalTest extends AbstractFunctionalTest {

    /**
     * Creates a PDPO log through the public endpoint and returns the persisted response body.
     *
     * @param businessIdentifier unique business identifier used for the request
     * @return the created PDPO log response
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    protected AddPdpoLogResponse createPdpoLog(String businessIdentifier)
        throws IOException, GeneralSecurityException {
        TestHttpClient.Response response = TestHttpClient.postJson(
            testUrl() + "/log/pdpo",
            objectMapper.writeValueAsBytes(addPdpoLogRequest(businessIdentifier))
        );

        assertThat(response.statusCode()).isEqualTo(201);
        return objectMapper.readValue(response.body(), AddPdpoLogResponse.class);
    }

    /**
     * Sends a valid PDPO log request with a generated business identifier.
     *
     * @return the raw HTTP response from the endpoint
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    protected TestHttpClient.Response addPdpoLog() throws IOException, GeneralSecurityException {
        return TestHttpClient.postJson(
            testUrl() + "/log/pdpo",
            objectMapper.writeValueAsBytes(addPdpoLogRequest(uniqueBusinessIdentifier()))
        );
    }

    /**
     * Searches for PDPO logs by business identifier through the test-support endpoint.
     *
     * @param businessIdentifier business identifier to search for
     * @return the matching PDPO log responses
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    protected List<AddPdpoLogResponse> searchLogs(String businessIdentifier)
        throws IOException, GeneralSecurityException {
        TestHttpClient.Response response = TestHttpClient.postJson(
            testUrl() + "/test-support/search",
            objectMapper.writeValueAsBytes(new SearchPdpoLogRequest().businessIdentifier(businessIdentifier))
        );

        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    /**
     * Sends a valid search request with a generated business identifier.
     *
     * @return the raw HTTP response from the endpoint
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    protected TestHttpClient.Response searchLogs() throws IOException, GeneralSecurityException {
        return TestHttpClient.postJson(
            testUrl() + "/test-support/search",
            objectMapper.writeValueAsBytes(
                new SearchPdpoLogRequest().businessIdentifier(uniqueBusinessIdentifier())
            )
        );
    }

    /**
     * Verifies that a response matches the standard feature-disabled problem detail contract.
     *
     * @param response HTTP response returned by a gated endpoint
     */
    protected void assertFeatureDisabled(TestHttpClient.Response response) {
        assertThat(response.statusCode()).isEqualTo(404);

        Map<String, Object> problemDetail = parseProblemDetail(response.body());

        assertThat(problemDetail)
            .containsEntry("title", "Feature Disabled")
            .containsEntry("detail", "The requested feature is not currently available")
            .containsEntry("type", "https://hmcts.gov.uk/problems/feature-disabled")
            .containsEntry("status", 404)
            .containsEntry("retriable", false);
        assertThat(problemDetail).containsKey("operation_id");
        assertThat(problemDetail).containsKey("instance");
    }

    /**
     * Creates a unique business identifier for functional test requests.
     *
     * @return unique identifier prefixed for release-1a scenarios
     */
    protected String uniqueBusinessIdentifier() {
        return "release-1a-" + UUID.randomUUID();
    }

    /**
     * Builds a representative PDPO log request for release-1a functional tests.
     *
     * @param businessIdentifier business identifier to include in the request
     * @return populated PDPO log request
     */
    private AddPdpoLogRequest addPdpoLogRequest(String businessIdentifier) {
        return new AddPdpoLogRequest()
            .createdBy(new ParticipantIdentifier().id("requestor-1").type("OPAL_USER_ID"))
            .businessIdentifier(businessIdentifier)
            .createdAt(OffsetDateTime.parse("2025-11-15T12:45:00Z"))
            .ipAddress("192.168.1.10")
            .category(CategoryEnum.DISCLOSURE)
            .recipient(new ParticipantIdentifier().id("recipient-42").type("EXTERNAL_SERVICE"))
            .individuals(List.of(new ParticipantIdentifier().id("person-1").type("DEFENDANT")));
    }

    private Map<String, Object> parseProblemDetail(String responseBody) {
        return objectMapper.readValue(
            responseBody,
            new TypeReference<>() {
            }
        );
    }
}
