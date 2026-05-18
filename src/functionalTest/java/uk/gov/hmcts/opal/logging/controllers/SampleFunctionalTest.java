package uk.gov.hmcts.opal.logging.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.logging.testsupport.AbstractFunctionalTest;
import uk.gov.hmcts.opal.logging.testsupport.TestHttpClient;

import java.io.IOException;
import java.security.GeneralSecurityException;

class SampleFunctionalTest extends AbstractFunctionalTest {
    /**
     * Verifies the root endpoint responds successfully for the functional test environment.
     *
     * @throws IOException if the response body cannot be read
     * @throws GeneralSecurityException if the HTTP client cannot establish a secure connection
     */
    @Test
    void functionalTest() throws IOException, GeneralSecurityException {
        TestHttpClient.Response response = TestHttpClient.get(testUrl() + "/");

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(response.body().startsWith("Welcome"));
    }
}
