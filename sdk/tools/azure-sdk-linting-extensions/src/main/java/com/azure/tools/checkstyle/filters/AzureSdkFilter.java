// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.tools.checkstyle.filters;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.Filter;

/**
 * A Checkstyle filter that filters out common Checkstyle violations that should be ignored by all SDKs.
 * <p>
 * The following violations are ignored:
 *
 * <ul>
 *     <li>ExternalDependencyExposed checks in implementation code</li>
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
    // The package name prefix for Azure SDK Checkstyle checks
    private static final String AZURE_SDK_CHECK_START = "com.azure.tools.checkstyle.checks.";

    // The ExternalDependencyExposed Azure SDK Checkstyle check
    private static final String EXTERNAL_DEPENDENCY_EXPOSED = AZURE_SDK_CHECK_START + "ExternalDependencyExposed";

    @Override
    public boolean accept(AuditEvent event) {
        boolean shouldSkip = isIgnoredImplementation(event) || isIgnoredSampleOrTest(event);

        return !shouldSkip;
    }

    private static boolean isIgnoredSampleOrTest(AuditEvent event) {
        String fileName = event.getFileName();

        // File isn't a Java file.
        if (!fileName.endsWith(".java")) {
            return false;
        }

        int srcStart = fileName.indexOf("src");

        // Doesn't contain .*src
        if (srcStart == -1) {
            return false;
        }

        char srcPathDelimiter = fileName.charAt(srcStart + 3 + 1);

        // Doesn't match the start .*src[/\\]
        if (srcPathDelimiter != '/' && srcPathDelimiter != '\\') {
            return false;
        }

        // Use the delimiter found previously to find the next path delimiter.
        int nextPathSegment = fileName.indexOf((srcPathDelimiter == '/') ? '/' : '\\', srcStart + 3 + 2);

        // There isn't another path segment.
        if (nextPathSegment == -1) {
            return false;
        }

        String folderName = fileName.substring(srcStart + 3 + 2, nextPathSegment);
        boolean isTestFile = folderName.startsWith("test");

        String violation = event.getViolation().getSourceName();

        if (violation.contains("Javadoc")) {
            // Ignore missing Javadoc comments in test code
            return true;
        } else if (isTestFile && violation.contains("AvoidStarImport")) {
            // Ignore star imports in test code
            return true;
        } else if (isTestFile && violation.contains("AvoidNestedBlocks")) {
            // Ignore nested blocks in test code
            return true;
        } else if (violation.startsWith(AZURE_SDK_CHECK_START)) {
            // Ignore Azure SDK Checkstyle checks in sample and test code
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
        String fileName = event.getFileName();

        // File isn't a Java file.
        if (!fileName.endsWith(".java")) {
            return false;
        }

        int srcStart = fileName.indexOf("src");

        // Doesn't contain .*src
        if (srcStart == -1) {
            return false;
        }

        char srcPathDelimiter = fileName.charAt(srcStart + 3 + 1);

        // Doesn't match the start .*src[/\\]
        if (srcPathDelimiter != '/' && srcPathDelimiter != '\\') {
            return false;
        }

        int implementationPosition = fileName.indexOf("implementation", srcStart + 2);

        // Doesn't contain implementation in the file path.
        if (implementationPosition == -1) {
            return false;
        }

        char implementationBeforePathDelimiter = fileName.charAt(implementationPosition - 1);
        char implementationAfterPathDelimiter = fileName.charAt(implementationPosition + 14 + 1);
        if ((implementationBeforePathDelimiter != '/' && implementationBeforePathDelimiter != '\\')
            || (implementationAfterPathDelimiter != '/' && implementationAfterPathDelimiter != '\\')) {
            return false;
        }

        return event.getViolation().getSourceName().startsWith(EXTERNAL_DEPENDENCY_EXPOSED);
    }
}
