// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.exception;

import com.generic.core.http.models.HttpRequest;

/**
 * The exception when an HTTP request fails.
 *
 * <p>Generally, these errors are safe to retry.</p>
 */
public final class HttpRequestException extends RuntimeException {
    /**
     * Information about the associated HTTP response.
     */
    private final transient HttpRequest request;
    private final HttpExceptionType type;

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message The exception message.
     * @param request The HTTP request sends to the service.
     * @param type The type of the exception.
     */
    public HttpRequestException(final String message, final HttpRequest request, final HttpExceptionType type) {
        super(message);

        this.request = request;
        this.type = type;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message The exception message.
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     * @param type The type of the exception.
     * @param cause The {@link Throwable} which caused the creation of this HttpRequestException.
     */
    public HttpRequestException(final String message, final HttpRequest request, final HttpExceptionType type,
                                final Throwable cause) {
        super(message, cause);

        this.request = request;
        this.type = type;
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message The exception message.
     * @param request The {@link HttpRequest} being sent when the exception occurred.
     * @param type The type of the exception.
     * @param cause The {@link Throwable} which caused the creation of this HttpRequestException.
     * @param enableSuppression Whether suppression is enabled or disabled.
     * @param writableStackTrace Whether the exception stack trace will be filled in.
     */
    public HttpRequestException(final String message, final HttpRequest request, final HttpExceptionType type,
                                final Throwable cause, final boolean enableSuppression,
                                final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

        this.request = request;
        this.type = type;
    }

    /**
     * Gets the {@link HttpRequest} being sent when the exception occurred.
     *
     * @return The {@link HttpRequest} being sent when the exception occurred.
     */
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Gets the type of the exception.
     *
     * @return The type of the exception.
     */
    public HttpExceptionType getType() {
        return type;
    }
}
