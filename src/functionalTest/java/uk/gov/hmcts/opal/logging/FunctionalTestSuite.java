package uk.gov.hmcts.opal.logging;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("uk.gov.hmcts.opal.logging.controllers")
@IncludeClassNamePatterns(".*FunctionalTest")
@ExcludeTags({"R1AOn", "R1AOff"})
public class FunctionalTestSuite {
}
