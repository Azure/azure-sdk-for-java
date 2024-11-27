// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * RetryExtension: The extension to retry tests upon failure.
 * TODO: @g2vinay, move this to azure-core-test.
 */
public class RetryExtension implements TestExecutionExceptionHandler, BeforeTestExecutionCallback {
    private static final int DEFAULT_MAX_RETRIES = 3;

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) {
        int maxRetries = getMaxRetries(context);

        executeWithRetries(() -> {
            try {
                return context.getTestMethod().get().invoke(context.getRequiredTestInstance());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, maxRetries, 1000, 2);

    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        // Initialize the retry counter before a test execution starts
        getStore(context).put(getTestKey(context), new AtomicInteger(0));
    }

    private int getMaxRetries(ExtensionContext context) {
        return context.getTestMethod()
            .flatMap(method -> Optional.ofNullable(method.getAnnotation(Retry.class)).map(Retry::maxRetries))
            .orElse(DEFAULT_MAX_RETRIES);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()));
    }

    private String getTestKey(ExtensionContext context) {
        return "retries-" + context.getUniqueId();
    }

    private static <T> T executeWithRetries(Supplier<T> logic, int maxRetries, long initialDelayMillis,
        double backoffFactor) {
        int attempt = 0;
        long delay = initialDelayMillis;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                return logic.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                if (attempt >= maxRetries) {
                    break;
                }
                System.out.printf("Attempt %d failed. Retrying in %d ms...%n", attempt, delay);
                try {
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                delay *= backoffFactor;
            }
        }

        throw new RuntimeException(lastException);
    }
}
