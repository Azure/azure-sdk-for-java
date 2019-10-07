// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content.
 */
public class SimpleResponse<T> implements Response<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final HttpHeaders headers;
    private final T value;

    /**
     * Creates a SimpleResponse.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param headers the headers of the HTTP response
     * @param value the deserialized value
     */
    public SimpleResponse(HttpRequest request, int statusCode, HttpHeaders headers, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Creates a SimpleResponse from a response and a value.
     *
     * @param response the response the needs to be mapped
     * @param value the value to put into the new response
     */
    public SimpleResponse(Response<?> response, T value) {
        this.request = response.getRequest();
        this.statusCode = response.getStatusCode();
        this.headers = response.getHeaders();
        this.value = value;
    }

    /**
     * @return the request which resulted in this RestResponse.
     */
    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * @return the status code of the HTTP response.
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
     * @return the deserialized value of the HTTP response.
     */
    @Override
    public T getValue() {
        return value;
    }
}
