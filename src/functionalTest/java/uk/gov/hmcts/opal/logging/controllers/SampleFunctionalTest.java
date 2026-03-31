package uk.gov.hmcts.opal.logging.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.opal.logging.testsupport.AbstractFunctionalTest;
import uk.gov.hmcts.opal.logging.testsupport.TestHttpClient;

import java.io.IOException;
import java.security.GeneralSecurityException;

class SampleFunctionalTest extends AbstractFunctionalTest {
    @Value("${TEST_URL:http://localhost:4065}")
    private String testUrl;

    @Test
    void functionalTest() throws IOException, GeneralSecurityException {
        TestHttpClient.Response response = TestHttpClient.get(testUrl + "/");

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(response.body().startsWith("Welcome"));
    }
}
