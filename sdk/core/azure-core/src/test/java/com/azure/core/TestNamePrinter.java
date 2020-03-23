// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * This class is a simple test class super class that will print out the name of the currently running test. This is
 * for test classes that don't extend 'TestBaseClass'.
 */
public class TestNamePrinter {
    @BeforeEach
    public void printTestName(TestInfo testInfo) {
        System.out.printf("========================= %s =========================%n", testInfo.getDisplayName());
    }
}
