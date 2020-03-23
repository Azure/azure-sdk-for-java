// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.util.Objects;

/**
 * This class is a simple test class super class that will print out the name of the currently running test. This is
 * for test classes that don't extend 'TestBaseClass'.
 */
public class TestNamePrinter {
    /**
     * Prints the name of the currently running test. This will print the fully qualified test name, if the test is a
     * parameterized test the display name will be printed in parentheses next to the fully qualified name.
     *
     * @param testInfo Information about the currently running test.
     */
    @BeforeEach
    public void printTestName(TestInfo testInfo) {
        String displayName = testInfo.getDisplayName();

        String testName = "";
        String fullyQualifiedTestName = "";
        if (testInfo.getTestMethod().isPresent()) {
            testName = testInfo.getTestMethod().get().getName();
            fullyQualifiedTestName = testInfo.getTestMethod().get().getDeclaringClass().getName() + "." + testName;
        }

        if (!Objects.equals(displayName, testName)) {
            System.out.printf("========================= %s (%s) =========================%n", fullyQualifiedTestName,
                displayName);
        } else {
            System.out.printf("========================= %s =========================%n", fullyQualifiedTestName);
        }
    }
}
