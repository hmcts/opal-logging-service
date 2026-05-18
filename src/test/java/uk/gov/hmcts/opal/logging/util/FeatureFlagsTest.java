package uk.gov.hmcts.opal.logging.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FeatureFlagsTest {

    @Test
    void defaultValuePropertyBuildsLaunchDarklyFallbackPropertyName() {
        assertThat(FeatureFlags.defaultValueProperty("release-1b"))
            .isEqualTo("launchdarkly.default-flag-values.release-1b");
    }

    @Test
    void release1aEnabledPropertyMatchesRelease1aDefaultValueProperty() {
        assertThat(FeatureFlags.RELEASE_1A_ENABLED_PROPERTY)
            .isEqualTo(FeatureFlags.defaultValueProperty(FeatureFlags.RELEASE_1A));
    }
}
