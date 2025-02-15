// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http;

/**
 * Contains the information needed to generate an exception type to be thrown or returned when a REST API returns an
 * error status code.
 */
public class UnexpectedExceptionInformation {
    private final Class<?> exceptionBodyClass;

    /**
     * Creates an {@link UnexpectedExceptionInformation} object with the given exception type and expected response
     * body.
     *
     * @param exceptionBodyClass The expected response body class.
     */
    public UnexpectedExceptionInformation(Class<?> exceptionBodyClass) {
        this.exceptionBodyClass = exceptionBodyClass == null ? Object.class : exceptionBodyClass;
    }

    /**
     * Get the exception's response body.
     *
     * @return The exception's response body.
     */
    public Class<?> getExceptionBodyClass() {
        return exceptionBodyClass;
    }
}
