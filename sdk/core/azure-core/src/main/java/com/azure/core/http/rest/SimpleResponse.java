// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;

/**
 * This class represents a simple HTTP response with a strongly-typed content.
 * It encapsulates the HTTP request that resulted in the response, the status code of the HTTP response,
 * the headers of the HTTP response, and the deserialized value of the HTTP response.
 *
 * <p>This class is useful when you want to work with the response of an HTTP request where the body of the response
 * is expected to be in a specific format (the generic type {@code T}).</p>
 *
 * @param <T> The type of the deserialized response content.
 */
public class SimpleResponse<T> implements Response<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final HttpHeaders headers;
    private final T value;

    /**
     * Creates a {@link SimpleResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param value The deserialized value of the HTTP response.
     */
    public SimpleResponse(HttpRequest request, int statusCode, HttpHeaders headers, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Creates a {@link SimpleResponse} from a response and a value.
     *
     * @param response The response that needs to be mapped.
     * @param value The value to put into the new response.
     */
    public SimpleResponse(Response<?> response, T value) {
        this.request = response.getRequest();
        this.statusCode = response.getStatusCode();
        this.headers = response.getHeaders();
        this.value = value;
    }

    /**
     * Gets the request which resulted in this {@link SimpleResponse}.
     *
     * @return The request which resulted in this {@link SimpleResponse}.
     */
    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Gets the status code of the HTTP response.
     *
     * @return The status code of the HTTP response.
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets the deserialized value of the HTTP response.
     *
     * @return The deserialized value of the HTTP response.
     */
    @Override
    public T getValue() {
        return value;
    }
}
