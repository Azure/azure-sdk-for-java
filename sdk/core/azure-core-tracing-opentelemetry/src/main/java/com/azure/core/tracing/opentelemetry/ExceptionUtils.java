// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// based on
// https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/instrumenter/DefaultErrorCauseExtractor.java
package com.azure.core.tracing.opentelemetry;

import reactor.core.Exceptions;

import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

final class ExceptionUtils {
    private static final Class<?> COMPLETION_EXCEPTION_CLASS;
    static {
        Class<?> completionExceptionClass = null;
        try {
            completionExceptionClass = Class.forName("java.util.concurrent.CompletionException");
        } catch (ClassNotFoundException e) {
            // Android level 21 does not support java.util.concurrent.CompletionException
        }
        COMPLETION_EXCEPTION_CLASS = completionExceptionClass;
    }

    public static Throwable unwrapError(Throwable error) {
        error = Exceptions.unwrap(error);
        if (error != null
            && error.getCause() != null
            && (error instanceof UncheckedIOException
                || error instanceof ExecutionException
                || isInstanceOfCompletionException(error)
                || error instanceof InvocationTargetException
                || error instanceof UndeclaredThrowableException)) {
            return unwrapError(error.getCause());
        }
        return error;
    }

    private static boolean isInstanceOfCompletionException(Throwable error) {
        return COMPLETION_EXCEPTION_CLASS != null && COMPLETION_EXCEPTION_CLASS.isInstance(error);
    }

    private ExceptionUtils() {
    }
}
