// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StorageImplUtilsTests {

    @ParameterizedTest
    @MethodSource("exceptionTypes")
    public void sendRequestThrowsException(Class<? extends Exception> exception) {

        Supplier<?> timeoutExceptionSupplier = generateSupplier(exception);
        CallableExceptionOperation operation = new CallableExceptionOperation(timeoutExceptionSupplier);

        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            StorageImplUtils.sendRequest(operation, Duration.ofSeconds(120), BlobStorageException.class);
        });

        assertNotNull(e.getCause());
        assertInstanceOf(exception, e.getCause());
    }

    private static Stream<Class<? extends Exception>> exceptionTypes() {
        return Stream.of(TimeoutException.class, RuntimeException.class, ExecutionException.class,
            InterruptedException.class);
    }

    private static class CallableExceptionOperation implements Callable<Object> {
        private final Supplier<?> exceptionSupplier;

        /**
         * Creates a new CallableExceptionOperation with the specified exception supplier.
         *
         * @param exceptionSupplier a {@link Supplier} that provides exception instances to be thrown
         *                         when {@link #call()} is invoked. The supplier should return instances
         *                         of supported exception types: {@link RuntimeException},
         *                         {@link TimeoutException}, {@link ExecutionException}, or
         *                         {@link InterruptedException}.
         */
        CallableExceptionOperation(Supplier<?> exceptionSupplier) {
            this.exceptionSupplier = exceptionSupplier;
        }

        @Override
        public Object call() throws Exception {
            Object e = exceptionSupplier.get();

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof TimeoutException) {
                throw (TimeoutException) e;
            } else if (e instanceof ExecutionException) {
                throw (ExecutionException) e;
            } else if (e instanceof InterruptedException) {
                throw (InterruptedException) e;
            }
            return new Object();
        }
    }

    private static Supplier<?> generateSupplier(Class<? extends Exception> exceptionType) {
        return () -> {
            try {
                if (exceptionType == ExecutionException.class) {
                    // ExecutionException requires a cause
                    return new ExecutionException("Test execution exception", new RuntimeException("Test cause"));
                } else {
                    // For other exceptions, use no-arg constructor
                    return exceptionType.getConstructor().newInstance();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to create exception instance", e);
            }
        };
    }

    private static final class BlobStorageException extends HttpResponseException {

        BlobStorageException(HttpResponse response) {
            super(response);
        }
    }
}
