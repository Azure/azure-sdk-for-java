// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.typespec.core.http;

import com.typespec.core.http.models.HttpRequest;
import com.typespec.core.http.models.HttpHeaders;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content.
 */
public class SimpleResponse<T> implements Response<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final HttpHeaders httpHeaders;
    private final T value;

    /**
     * Creates a {@link SimpleResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param httpHeaders The headers of the HTTP response.
     * @param value The deserialized value of the HTTP response.
     */
    public SimpleResponse(HttpRequest request, int statusCode, HttpHeaders httpHeaders, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.httpHeaders = httpHeaders;
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
        this.httpHeaders = response.getHeaders();
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
        return httpHeaders;
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
