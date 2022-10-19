// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import okhttp3.Dispatcher;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class TestUtils {
    private static final AtomicInteger QUIET_DISPATCHER_THREAD = new AtomicInteger();

    /**
     * Creates an OkHttp Dispatcher that doesn't use System.out to print the uncaught exception in the Thread uncaught
     * exception handler when the exception matches what the test is expected to throw. This reduces OkHttp stack traces
     * in testing logs to help differentiate expected behaviors from truly unexpected exceptions.
     *
     * @param expectedErrorType The type of exception the test is throwing.
     * @param expectedErrorMessage The message of the exception the test is throwing.
     * @return An OkHttp Dispatcher that will quiet the expected exceptions.
     */
    public static Dispatcher createQuietDispatcher(Class<? extends Throwable> expectedErrorType,
        String expectedErrorMessage) {
        // Most tests will only run a single network call, so it's fine to use a single thread executor that won't
        // allow for concurrent requests.
        return new Dispatcher(Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "AzureQuietDispatch-" + QUIET_DISPATCHER_THREAD.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, e) -> {
                if (e.getClass() == expectedErrorType
                    && e.getMessage() != null
                    && e.getMessage().contains(expectedErrorMessage)) {
                    return;
                }

                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(t, e);
            });

            return thread;
        }));
    }

    private TestUtils() {
    }
}
