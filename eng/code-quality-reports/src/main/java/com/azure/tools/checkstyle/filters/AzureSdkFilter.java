// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.tools.checkstyle.filters;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.Filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Checkstyle filter that filters out common Checkstyle violations that should be ignored by all SDKs.
 * <p>
 * The following violations are ignored:
 *
 * <ul>
 *     <li>Missing Javadoc comments in sample and test code</li>
 *     <li>Star imports in test code</li>
 *     <li>Nested blocks in test code</li>
 *     <li>Azure SDK Checkstyle checks in sample and test code</li>
 *     <li>Missing package-info in sample and test code</li>
 *     <li>Line length in sample and test code</li>
 *     <li>Equals avoid null in sample code</li>
 * </ul>
 */
public class AzureSdkFilter implements Filter {
    // Pattern that matches sample, test, and test-shared files.
    // This will capture the file type (sample, test, or test-shared) in group 1, which removes the need for multiple
    // Patterns to match whether the file is a sample, test, or test-shared file to improve performance.
    private static final Pattern SAMPLE_OR_TEST_FILE_PATTERN
        = Pattern.compile(".*src[/\\\\](samples|test|test-shared)[/\\\\]java[/\\\\].*\\.java$");

    // The package name prefix for Azure SDK Checkstyle checks
    private static final String AZURE_SDK_CHECK_START = "com.azure.tools.checkstyle.checks.";

    @Override
    public boolean accept(AuditEvent event) {
        Matcher matcher = SAMPLE_OR_TEST_FILE_PATTERN.matcher(event.getFileName());

        if (!matcher.matches()) {
            // Not a test or sample file, so don't filter
            return true;
        }

        boolean isTestFile = matcher.group(1).startsWith("test");

        String violation = event.getViolation().getSourceName();

        if (violation.contains("Javadoc")) {
            // Ignore missing Javadoc comments in test code
            return false;
        } else if  (isTestFile && violation.contains("AvoidStarImport")) {
            // Ignore star imports in test code
            return false;
        } else if (isTestFile && violation.contains("AvoidNestedBlocks")) {
            // Ignore nested blocks in test code
            return false;
        } else if (violation.startsWith(AZURE_SDK_CHECK_START)) {
            // Ignore Azure SDK Checkstyle checks in sample and test code
            return false;
        } else if (violation.contains("LineLength")) {
            // Ignore line length in sample and test code
            return false;
        } else if (!isTestFile && violation.contains("EqualsAvoidNull")) {
            // Ignore equals avoid null in sample code
            return false;
        } else {
            return true;
        }
    }
}
