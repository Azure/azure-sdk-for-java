// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.TestAbortedException;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryExtension implements TestExecutionExceptionHandler, BeforeTestExecutionCallback {

    private static final int DEFAULT_MAX_RETRIES = 3;

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        // Fetch or initialize retry counter
        AtomicInteger retries = getRetries(context);
        int maxRetries = getMaxRetries(context);

        if (retries.incrementAndGet() <= maxRetries) {
            System.out.println("Retrying test: " + context.getDisplayName() + " (Attempt " + retries.get() + ")");
            throw new TestAbortedException("Retrying test...");
        } else {
            System.out.println("Exceeded retries for test: " + context.getDisplayName());
            throw throwable; // No more retries; propagate the original exception
        }
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        // Reset the retry counter before a new test execution
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(getTestKey(context), new AtomicInteger(0));
    }

    private AtomicInteger getRetries(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.GLOBAL)
            .getOrComputeIfAbsent(getTestKey(context), key -> new AtomicInteger(0), AtomicInteger.class);
    }

    private int getMaxRetries(ExtensionContext context) {
        return context.getTestMethod()
            .flatMap(method -> method.getAnnotation(Retry.class) != null
                ? Optional.of(method.getAnnotation(Retry.class).maxRetries())
                : Optional.empty())
            .orElse(DEFAULT_MAX_RETRIES);
    }

    private String getTestKey(ExtensionContext context) {
        return context.getUniqueId();
    }
}
