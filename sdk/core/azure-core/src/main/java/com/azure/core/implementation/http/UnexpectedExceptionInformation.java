// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.exception.HttpResponseException;

import java.lang.reflect.Method;

/**
 * Contains the information needed to generate a exception type to be thrown or returned when a REST API returns
 * an error status code.
 */
public class UnexpectedExceptionInformation {
    private static final String EXCEPTION_BODY_METHOD = "getValue";
    private final Class<? extends HttpResponseException> exceptionType;
    private final Class<?> exceptionBodyType;

    /**
     * Creates an UnexpectedExceptionInformation object with the given exception type and expected response body.
     * @param exceptionType Exception type to be thrown.
     */
    public UnexpectedExceptionInformation(Class<? extends HttpResponseException> exceptionType) {
        this.exceptionType = exceptionType;

        // Should always have a value() method. Register Object as a fallback plan.
        Class<?> exceptionBodyType = Object.class;
        try {
            final Method exceptionBodyMethod = exceptionType.getDeclaredMethod(EXCEPTION_BODY_METHOD);
            exceptionBodyType = exceptionBodyMethod.getReturnType();
        } catch (NoSuchMethodException e) {
            // no-op
        }
        this.exceptionBodyType = exceptionBodyType;
    }

    /**
     * @return the exception type.
     */
    public Class<? extends HttpResponseException> getExceptionType() {
        return exceptionType;
    }

    /**
     * @return the exception's response body.
     */
    public Class<?> getExceptionBodyType() {
        return exceptionBodyType;
    }
}
