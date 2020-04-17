// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.testing;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * JUnit 5 extension class which reports on testing running and simple metrics about the test such as run time.
 */
public class AzureTestWatcher implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        String displayName = extensionContext.getDisplayName();
        getStore(extensionContext).put(extensionContext.getRequiredTestMethod(), System.currentTimeMillis());

        String testName = "";
        String fullyQualifiedTestName = "";
        if (extensionContext.getTestMethod().isPresent()) {
            Method method = extensionContext.getTestMethod().get();
            testName = method.getName();
            fullyQualifiedTestName = method.getDeclaringClass().getName() + "." + testName;
        }

        if (!Objects.equals(displayName, testName)) {
            System.out.printf("Starting test %s (%s)%n", fullyQualifiedTestName, displayName);
        } else {
            System.out.printf("Starting test %s%n", fullyQualifiedTestName);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        long start = getStore(context).remove(testMethod, long.class);
        long duration = System.currentTimeMillis() - start;

        System.out.printf("Test took %d ms.", duration);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context));
    }
}
