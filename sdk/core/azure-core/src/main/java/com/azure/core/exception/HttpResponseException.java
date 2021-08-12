// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import com.azure.core.http.HttpResponse;

/**
 * The exception thrown when an unsuccessful response is received with http status code (e.g. 3XX, 4XX, 5XX) from the
 * service request.
 */
public class HttpResponseException extends AzureException {

    /**
     * The HTTP response value.
     */
    private final Object value;

    /**
     * Information about the associated HTTP response.
     */
    private final HttpResponse response;

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param response The {@link HttpResponse} received that is associated to the exception.
     */
    public HttpResponseException(final HttpResponse response) {
        super();
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link HttpResponse} received that is associated to the exception.
     */
    public HttpResponseException(final String message, final HttpResponse response) {
        super(message);
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param response The {@link HttpResponse} received that is associated to the exception.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public HttpResponseException(final HttpResponse response, final Throwable cause) {
        super(cause);
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link HttpResponse} received that is associated to the exception.
     * @param value The deserialized response value.
     */
    public HttpResponseException(final String message, final HttpResponse response, final Object value) {
        super(message);
        this.value = value;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link HttpResponse} received that is associated to the exception.
     * @param error The {@link HttpResponseError} associated with this exception.
     */
    public HttpResponseException(final String message, final HttpResponse response, final HttpResponseError error) {
        super(message);
        this.value = error;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link HttpResponse} received that is associated to the exception.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public HttpResponseException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, cause);
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link HttpResponse} received that is associated to the exception.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     * @param enableSuppression Whether suppression is enabled or disabled.
     * @param writableStackTrace Whether the exception stack trace will be filled in.
     */
    public HttpResponseException(final String message, final HttpResponse response, final Throwable cause,
        final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.value = null;
        this.response = response;
    }

    /**
     * @return The {@link HttpResponse} received that is associated to the exception.
     */
    public HttpResponse getResponse() {
        return response;
    }

    /**
     * @return The deserialized HTTP response value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the {@link HttpResponseError} containing the error details that caused this exception.
     *
     * @return The {@link HttpResponseError} containing the error details that caused this exception.
     * @throws IllegalStateException if the error details cannot be mapped to an instance of {@link HttpResponseError}.
     */
    public HttpResponseError getError() {
        if (value == null) {
            return null;
        }
        if (value instanceof HttpResponseError) {
            return (HttpResponseError) value;
        }
        throw new IllegalStateException("The error in this exception instance is not an HttpResponseError. Use " +
                "'getValue()' method instead");
    }
}
