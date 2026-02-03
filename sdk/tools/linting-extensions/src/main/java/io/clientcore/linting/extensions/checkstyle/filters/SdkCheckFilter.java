// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.linting.extensions.checkstyle.filters;

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
 *     <li>ExternalDependencyExposed checks in implementation code</li>
 *     <li>Missing Javadoc comments in implementation, sample, and test code</li>
 *     <li>Star imports in test code</li>
 *     <li>Nested blocks in test code</li>
 *     <li>SDK Checkstyle checks in sample and test code</li>
 *     <li>Missing package-info in sample and test code</li>
 *     <li>Line length in sample and test code</li>
 *     <li>Equals avoid null in sample code</li>
 * </ul>
 */
public class SdkCheckFilter implements Filter {
    // Pattern that matches sample, test, and test-shared files.
    // This will capture the file type (sample, test, or test-shared) in group 1, which removes the need for multiple
    // Patterns to match whether the file is a sample, test, or test-shared file to improve performance.
    private static final Pattern SAMPLE_OR_TEST_FILE_PATTERN
        = Pattern.compile(".*src[/\\\\](samples|test|test-shared)[/\\\\]java[/\\\\].*\\.java$");

    // Pattern that matches implementation files.
    private static final Pattern IMPLEMENTATION_FILE_PATTERN
        = Pattern.compile(".*src[/\\\\].*[/\\\\]implementation[/\\\\].*\\.java$");

    // The package name prefix for SDK Checkstyle checks
    private static final String SDK_CHECK_START = "io.clientcore.linting.extensions.checkstyle.checks.";

    // The ExternalDependencyExposed SDK Checkstyle check
    private static final String EXTERNAL_DEPENDENCY_EXPOSED = SDK_CHECK_START + "ExternalDependencyExposed";

    /**
     * Creates a new instance of {@link SdkCheckFilter}.
     */
    public SdkCheckFilter() {
    }

    @Override
    public boolean accept(AuditEvent event) {
        boolean shouldSkip = isIgnoredImplementation(event) || isIgnoredSampleOrTest(event);

        return !shouldSkip;
    }

    private static boolean isIgnoredSampleOrTest(AuditEvent event) {
        Matcher matcher = SAMPLE_OR_TEST_FILE_PATTERN.matcher(event.getFileName());

        if (!matcher.matches()) {
            // Not a test or sample file, so don't filter
            return false;
        }

        boolean isTestFile = matcher.group(1).startsWith("test");

        String violation = event.getViolation().getSourceName();
        if (violation.toLowerCase().contains("javadoc")) {
            // Ignore missing Javadoc comments in test code
            return true;
        } else if (isTestFile && violation.contains("AvoidStarImport")) {
            // Ignore star imports in test code
            return true;
        } else if (isTestFile && violation.contains("AvoidNestedBlocks")) {
            // Ignore nested blocks in test code
            return true;
        } else if (violation.startsWith(SDK_CHECK_START)) {
            // Ignore SDK Checkstyle checks in sample and test code
            return true;
        } else if (violation.contains("LineLength")) {
            // Ignore line length in sample and test code
            return true;
        } else if (!isTestFile && violation.contains("EqualsAvoidNull")) {
            // Ignore equals avoid null in sample code
            return true;
        }

        return false;
    }

    private static boolean isIgnoredImplementation(AuditEvent event) {
        Matcher matcher = IMPLEMENTATION_FILE_PATTERN.matcher(event.getFileName());

        if (!matcher.matches()) {
            // Not an implementation file, so don't filter
            return false;
        }

        String violation = event.getViolation().getSourceName();

        return violation.startsWith(EXTERNAL_DEPENDENCY_EXPOSED) || violation.toLowerCase().contains("javadoc");
    }
}
