// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.implementation.TestRunMetrics;
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

        String testName = "";
        String fullyQualifiedTestName = "";
        if (extensionContext.getTestMethod().isPresent()) {
            Method method = extensionContext.getTestMethod().get();
            testName = method.getName();
            fullyQualifiedTestName = method.getDeclaringClass().getName() + "." + testName;
        }

        StringBuilder logPrefixBuilder = new StringBuilder("Starting test ")
            .append(fullyQualifiedTestName);

        if (!Objects.equals(displayName, testName)) {
            logPrefixBuilder.append("(")
                .append(displayName)
                .append(")");
        }

        logPrefixBuilder.append(",");

        getStore(extensionContext).put(extensionContext.getRequiredTestMethod(),
            new TestRunMetrics(logPrefixBuilder.toString(), System.currentTimeMillis()));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        TestRunMetrics testInformation = getStore(context)
            .remove(context.getRequiredTestMethod(), TestRunMetrics.class);
        long duration = System.currentTimeMillis() - testInformation.getStartMillis();

        System.out.printf("%s completed in %d ms.%n", testInformation.getLogPrefix(), duration);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(AzureTestWatcher.class, context));
    }
}
