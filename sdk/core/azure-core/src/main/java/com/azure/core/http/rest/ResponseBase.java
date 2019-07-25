// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;

/**
 * The response of a REST request.
 *
 * @param <H> The deserialized type of the response headers.
 * @param <T> The deserialized type of the response value, available from {@link #value()}.
 */
public class ResponseBase<H, T> implements Response<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final H deserializedHeaders;
    private final HttpHeaders headers;
    private final T value;

    /**
     * Create ResponseBase.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param headers the headers of the HTTP response
     * @param deserializedHeaders the deserialized headers of the HTTP response
     * @param value the deserialized value
     */
    public ResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, T value, H deserializedHeaders) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.deserializedHeaders = deserializedHeaders;
        this.value = value;
    }

    /**
     * @return the request which resulted in this RestResponseBase.
     */
    @Override
    public HttpRequest request() {
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int statusCode() {
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders headers() {
        return headers;
    }

    /**
     * Get the headers from the HTTP response, transformed into the header type H.
     *
     * @return an instance of header type H, containing the HTTP response headers.
     */
    public H deserializedHeaders() {
        return deserializedHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T value() {
        return value;
    }
}
