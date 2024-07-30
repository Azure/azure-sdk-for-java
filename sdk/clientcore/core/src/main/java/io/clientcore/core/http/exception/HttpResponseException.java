// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.exception;

import io.clientcore.core.http.models.Response;

/**
 * The exception thrown when an unsuccessful response is received with http status code (e.g. {@code 3XX}, {@code 4XX},
 * {@code 5XX}) from the service request.
 */
public final class HttpResponseException extends RuntimeException {
    /**
     * The HTTP response value.
     */
    private final Object value;

    /**
     * Information about the associated HTTP response.
     */
    private final Response<?> response;

    /**
     * The type of the exception.
     */
    private final HttpExceptionType type;

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link Response} received that is associated to the exception.
     * @param type The {@link HttpExceptionType type} of the exception.
     * @param value The deserialized response value.
     */
    public HttpResponseException(final String message, final Response<?> response, final HttpExceptionType type,
                                 final Object value) {
        super(message);

        this.value = value;
        this.response = response;
        this.type = type;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link Response} received that is associated to the exception.
     * @param type The {@link HttpExceptionType type} of the exception.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public HttpResponseException(final String message, final Response<?> response, final HttpExceptionType type,
                                 final Throwable cause) {
        super(message, cause);

        this.value = null;
        this.response = response;
        this.type = type;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link Response} received that is associated to the exception.
     * @param type The {@link HttpExceptionType type} of the exception.
     * @param value The deserialized response value.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     * @param enableSuppression Whether suppression is enabled or disabled.
     * @param writableStackTrace Whether the exception stack trace will be filled in.
     */
    public HttpResponseException(final String message, final Response<?> response, final HttpExceptionType type,
                                 final Object value, final Throwable cause, final boolean enableSuppression,
                                 final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

        this.value = value;
        this.response = response;
        this.type = type;
    }

    /**
     * Gets the {@link Response} received that is associated to the exception.
     *
     * @return The {@link Response} received that is associated to the exception.
     */
    public Response<?> getResponse() {
        return response;
    }

    /**
     * Gets the deserialized HTTP response value.
     *
     * @return The deserialized HTTP response value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets the {@link HttpExceptionType type} of the exception.
     *
     * @return The {@link HttpExceptionType type} of the exception.
     */
    public HttpExceptionType getType() {
        return type;
    }
}
