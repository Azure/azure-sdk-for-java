// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.exception;

import io.clientcore.core.http.models.HttpRequest;

/**
 * <p>The {@code HttpRequestException} that represents an exception thrown when an HTTP request fails.</p>
 *
 * <p>This exception is typically thrown when the client sends an HTTP request to the Azure service, but the service
 * is unable to process the request.</p>
 *
 * @see com.azure.core.exception
 * @see com.azure.core.exception.AzureException
 * @see HttpRequest
 */
public class HttpRequestException extends AzureException {

    /**
     * Information about the associated HTTP response.
     */
    private final transient HttpRequest request;

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     */
    public HttpRequestException(final HttpRequest request) {
        super();
        this.request = request;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message The exception message.
     * @param request the HTTP request sends to the Azure service
     */
    public HttpRequestException(final String message, final HttpRequest request) {
        super(message);
        this.request = request;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     * @param cause The {@link Throwable} which caused the creation of this HttpRequestException.
     */
    public HttpRequestException(final HttpRequest request, final Throwable cause) {
        super(cause);
        this.request = request;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message The exception message.
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     * @param cause The {@link Throwable} which caused the creation of this HttpRequestException.
     */
    public HttpRequestException(final String message, final HttpRequest request, final Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message The exception message.
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     * @param cause The {@link Throwable} which caused the creation of this HttpRequestException.
     * @param enableSuppression Whether suppression is enabled or disabled.
     * @param writableStackTrace Whether the exception stack trace will be filled in.
     */
    public HttpRequestException(final String message, final HttpRequest request, final Throwable cause,
        final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.request = request;
    }

    /**
     * Gets the {@link HttpRequest} being sent when the exception occurred.
     *
     * @return The {@link HttpRequest} being sent when the exception occurred.
     */
    public HttpRequest getRequest() {
        return request;
    }

}
