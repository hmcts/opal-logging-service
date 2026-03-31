package uk.gov.hmcts.opal.logging.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.opal.logging.testsupport.AbstractSmokeTest;
import uk.gov.hmcts.opal.logging.testsupport.TestHttpClient;

import java.io.IOException;
import java.security.GeneralSecurityException;

class SampleSmokeTest extends AbstractSmokeTest {
    @Value("${TEST_URL:http://localhost:4065}")
    private String testUrl;

    @Test
    void smokeTest() throws IOException, GeneralSecurityException {
        TestHttpClient.Response response = TestHttpClient.get(testUrl + "/");

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(response.body().startsWith("Welcome"));
    }
}
