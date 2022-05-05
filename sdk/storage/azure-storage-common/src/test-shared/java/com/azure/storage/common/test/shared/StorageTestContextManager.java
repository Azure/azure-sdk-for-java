// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;

import java.lang.reflect.Method;

/**
 * Storage test context manager.
 */
public final class StorageTestContextManager extends TestContextManager {
    private final String testName;

    /**
     * Constructs a {@link TestContextManager} based on the test method.
     *
     * @param testMethod Test method being run.
     * @param testMode The {@link TestMode} the test is running in.
     * @param testName Name of the test.
     */
    public StorageTestContextManager(Method testMethod, TestMode testMode, String testName) {
        super(testMethod, testMode);

        this.testName = testName;
    }

    @Override
    public String getTestName() {
        return testName;
    }
}
