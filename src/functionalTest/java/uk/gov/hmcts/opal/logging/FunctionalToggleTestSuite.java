package uk.gov.hmcts.opal.logging;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("uk.gov.hmcts.opal.logging.controllers")
@IncludeClassNamePatterns(".*FunctionalTest")
@IncludeTags("R1AOff")
public class FunctionalToggleTestSuite {
}
