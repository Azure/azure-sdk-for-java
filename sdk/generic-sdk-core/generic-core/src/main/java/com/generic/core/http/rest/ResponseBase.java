// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.generic.core.http.rest;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.models.Headers;

/**
 * The response of a REST request.
 *
 * @param <H> The deserialized type of the response headers.
 * @param <T> The deserialized type of the response value, available from {@link Response#getValue()}.
 */
public class ResponseBase<H, T> implements Response<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final H deserializedHeaders;
    private final Headers headers;
    private final T value;

    /**
     * Creates a {@link ResponseBase}.
     *
     * @param request The HTTP request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param deserializedHeaders The deserialized headers of the HTTP response.
     * @param value The deserialized value of the HTTP response.
     */
    public ResponseBase(HttpRequest request, int statusCode, Headers headers, T value, H deserializedHeaders) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.deserializedHeaders = deserializedHeaders;
        this.value = value;
    }

    /**
     * Gets The request which resulted in this {@link ResponseBase}.
     *
     * @return The request which resulted in this {@link ResponseBase}.
     */
    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Headers getHeaders() {
        return headers;
    }

    /**
     * Get the headers from the HTTP response, transformed into the header type, {@code H}.
     *
     * @return An instance of header type {@code H}, deserialized from the HTTP response headers.
     */
    public H getDeserializedHeaders() {
        return deserializedHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue() {
        return value;
    }
}
