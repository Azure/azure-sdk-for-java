// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;

import java.io.Closeable;
import java.io.IOException;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content, available from {@link #getValue()}.
 */
public class Response<T> implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(Response.class);
    private final HttpHeaders headers;
    private final HttpRequest request;
    private final int statusCode;
    private final T value;

    /**
     * Creates an instance of {@link Response}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link Response}.
     * @param statusCode The response status code.
     * @param headers The response {@link HttpHeaders}.
     * @param value The deserialized value of the response.
     */
    public Response(HttpRequest request, int statusCode, HttpHeaders headers, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Gets the response status code.
     *
     * @return The status code of the response.
     */
    public final int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the headers from the response.
     *
     * @return The response headers.
     */
    public final HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets the request which resulted in this response.
     *
     * @return The request.
     */
    public final HttpRequest getRequest() {
        return request;
    }

    /**
     * Gets the deserialized value of this response.
     *
     * @return The deserialized value of this response.
     */
    public T getValue() {
        return value;
    }

    /**
     * If {@link #getValue()} is a {@link Closeable} type, this method will close it.
     *
     * @throws CoreException If an error occurs while closing the response.
     */
    @Override
    public void close() {
        if (value instanceof Closeable) {
            try {
                ((Closeable) value).close();
            } catch (IOException e) {
                throw LOGGER.throwableAtError().log(e, (msg, cause) -> CoreException.from(msg, cause, false));
            }
        }
    }
}
