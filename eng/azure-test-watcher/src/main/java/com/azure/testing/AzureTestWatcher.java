package com.azure.testing;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Objects;

public class AzureTestWatcher implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
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
            System.out.printf("Starting test %s (%s)%n", fullyQualifiedTestName,
                displayName);
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
