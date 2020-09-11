// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

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
            new TestInformation(logPrefixBuilder.toString(), System.currentTimeMillis()));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        TestInformation testInformation = getStore(context)
            .remove(context.getRequiredTestMethod(), TestInformation.class);
        long duration = System.currentTimeMillis() - testInformation.startMillis;

        System.out.printf("%s completed in %d ms.%n", testInformation.logPrefix, duration);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(AzureTestWatcher.class, context));
    }

    private static final class TestInformation {
        private final String logPrefix;
        private final long startMillis;

        private TestInformation(String logPrefix, long startMillis) {
            this.logPrefix = logPrefix;
            this.startMillis = startMillis;
        }
    }
}
