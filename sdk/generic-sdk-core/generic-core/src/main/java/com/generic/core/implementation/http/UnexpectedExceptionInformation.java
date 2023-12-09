// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http;

import com.generic.core.http.exception.HttpExceptionType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Contains the information needed to generate an exception type to be thrown or returned when a REST API returns an
 * error status code.
 */
public class UnexpectedExceptionInformation {
    private final Class<?> exceptionBodyClass;
    private final HttpExceptionType exceptionType;

    /**
     * Creates an {@link UnexpectedExceptionInformation} object with the given exception type and expected response
     * body.
     *
     * @param exceptionType The type of exception to be thrown.
     */
    public UnexpectedExceptionInformation(HttpExceptionType exceptionType, Class<?> exceptionBodyClass) {
        this.exceptionType = exceptionType;
        this.exceptionBodyClass = exceptionBodyClass == null ? Object.class : exceptionBodyClass;
    }

    /**
     * @return The exception's response body.
     */
    public Class<?> getExceptionBodyClass() {
        return exceptionBodyClass;
    }

    public HttpExceptionType getExceptionType() {
        return exceptionType;
    }
}
