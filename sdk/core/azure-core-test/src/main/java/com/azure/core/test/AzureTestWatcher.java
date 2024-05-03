// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.azure.core.test.TestBase.shouldLogExecutionStatus;
import static com.azure.core.test.implementation.TestingHelpers.getTestName;

/**
 * JUnit 5 extension class which reports on testing running and simple metrics about the test such as run time.
 */
public class AzureTestWatcher implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    /**
     * Creates an instance of {@link AzureTestWatcher}.
     */
    public AzureTestWatcher() {
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        // If the test class is an instance of TestBase or is a subtype of TestBase, then we don't need to track
        // anything here as TestBase handles this logic in it's Before and After test methods.
        Class<?> clazz = context.getTestClass().orElse(null);
        if (clazz != null && TestBase.class.isAssignableFrom(clazz)) {
            return;
        }

        // Check if test debugging is enabled to determine whether logging should happen.
        if (!shouldLogExecutionStatus()) {
            return;
        }

        String testName = getTestName(context.getTestMethod(), context.getDisplayName(), context.getTestClass());
        System.out.println("Starting test " + testName + ".");

        getStore(context).put(context.getRequiredTestMethod(), System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        // If the test class is an instance of TestBase or is a subtype of TestBase, then we don't need to track
        // anything here as TestBase handles this logic in it's Before and After test methods.
        Class<?> clazz = context.getTestClass().orElse(null);
        if (clazz != null && TestBase.class.isAssignableFrom(clazz)) {
            return;
        }

        // Check if test debugging is enabled to determine whether logging should happen.
        if (!shouldLogExecutionStatus()) {
            return;
        }

        long startMillis = getStore(context).remove(context.getRequiredTestMethod(), long.class);
        long duration = System.currentTimeMillis() - startMillis;

        String testName = getTestName(context.getTestMethod(), context.getDisplayName(), context.getTestClass());
        System.out.println("Finished test " + testName + " in " + duration + " ms.");
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(AzureTestWatcher.class, context));
    }
}
