/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.common.http.rest;

import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpRequest;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content.
 */
public class SimpleRestResponse<T> implements RestResponse<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final HttpHeaders headers;
    private final T body;

    /**
     * Creates RestResponse.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param headers the headers of the HTTP response
     * @param body the deserialized body
     */
    public SimpleRestResponse(HttpRequest request, int statusCode, HttpHeaders headers, T body) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    /**
     * @return the request which resulted in this RestResponse.
     */
    @Override
    public HttpRequest request() {
        return request;
    }

    /**
     * @return the status code of the HTTP response.
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
     * @return the deserialized body of the HTTP response.
     */
    @Override
    public T body() {
        return body;
    }
}
