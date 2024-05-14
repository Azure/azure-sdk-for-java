// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import okhttp3.Dispatcher;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
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
     *
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

    /**
     * Asserts that two arrays are equal.
     * <p>
     * This method is similar to JUnit's {@link Assertions#assertArrayEquals(byte[], byte[])} except that it takes
     * advantage of hardware intrinsics offered by the JDK to optimize comparing the byte arrays.
     * <p>
     * If the arrays aren't equal this will call {@link Assertions#assertArrayEquals(byte[], byte[])} to take advantage
     * of the better error message, but this is the exceptional case and worth the double comparison performance hit.
     *
     * @param expected The expected byte array.
     * @param actual The actual byte array.
     */
    public static void assertArraysEqual(byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            Assertions.assertArrayEquals(expected, actual);
        }
    }

    private TestUtils() {
    }
}
