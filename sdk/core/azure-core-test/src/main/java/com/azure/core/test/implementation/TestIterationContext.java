// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hook into JUnit 5's test infrastructure which determines the test iteration.
 */
public final class TestIterationContext implements BeforeEachCallback {
    private static final Pattern TEST_ITERATION_PATTERN = Pattern.compile("test-template-invocation:#(\\d+)");

    private Integer testIteration;

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        Matcher matcher = TEST_ITERATION_PATTERN.matcher(extensionContext.getUniqueId());
        if (matcher.find()) {
            testIteration = Integer.valueOf(matcher.group(1));
        }
    }

    /**
     * Gets the current test iteration.
     *
     * @return The current test iteration.
     */
    public Integer getTestIteration() {
        return testIteration;
    }
}
