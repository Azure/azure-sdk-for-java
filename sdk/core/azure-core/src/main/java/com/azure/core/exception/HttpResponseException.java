// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import com.azure.core.http.HttpResponse;

/**
 * <p>The {@code HttpResponseException} represents an exception thrown when an unsuccessful HTTP response is received
 * from a service request.</p>
 *
 * <p>This exception is typically thrown when the service responds with a non-success status code
 * (e.g., 3XX, 4XX, 5XX).</p>
 *
 * <p>This class also provides methods to get the {@link HttpResponse} that was received when the exception occurred and
 * the deserialized HTTP response value.</p>
 *
 * @see com.azure.core.exception
 * @see com.azure.core.exception.AzureException
 * @see com.azure.core.http.HttpResponse
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
     * Gets the {@link HttpResponse} received that is associated to the exception.
     *
     * @return The {@link HttpResponse} received that is associated to the exception.
     */
    public HttpResponse getResponse() {
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
}
