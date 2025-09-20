// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.exception.HttpResponseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StorageImplUtilsTests {

    @ParameterizedTest
    @MethodSource("exceptionCallables")
    void sendRequestThrowsExceptions(Callable<?> operation, Class<? extends Exception> expectedCauseType) {
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> StorageImplUtils.sendRequest(operation, Duration.ofSeconds(120), HttpResponseException.class));

        assertNotNull(e.getCause());
        assertInstanceOf(expectedCauseType, e.getCause());
    }

    private static Stream<Arguments> exceptionCallables() {
        Callable<Object> timeoutCallable = () -> {
            throw new TimeoutException();
        };

        Callable<Object> runtimeCallable = () -> {
            throw new RuntimeException("rt");
        };

        Callable<Object> executionCallable = () -> {
            throw new ExecutionException("exec", new RuntimeException("inner"));
        };

        Callable<Object> interruptedCallable = () -> {
            throw new InterruptedException("interrupted");
        };

        return Stream.of(Arguments.of(timeoutCallable, TimeoutException.class),
            Arguments.of(runtimeCallable, RuntimeException.class),
            Arguments.of(executionCallable, ExecutionException.class),
            Arguments.of(interruptedCallable, InterruptedException.class));
    }
}
