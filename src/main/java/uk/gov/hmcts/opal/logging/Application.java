package uk.gov.hmcts.opal.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.opal.common.controllers.advice.OpalGlobalExceptionHandler;

@SpringBootApplication(
    scanBasePackages = {
        "uk.gov.hmcts.opal.logging",
        "uk.gov.hmcts.opal.common.launchdarkly",
        "uk.gov.hmcts.common",
    }
)
@Import(OpalGlobalExceptionHandler.class)
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
