/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3;

import com.microsoft.rest.v3.http.HttpRequest;

import java.util.Map;

/**
 * The response of a REST request.
 *
 * @param <THeaders> The deserialized type of the response headers.
 * @param <TBody> The deserialized type of the response body.
 */
public class RestResponse<THeaders, TBody> {
    private final HttpRequest request;
    private final int statusCode;
    private final THeaders headers;
    private final Map<String, String> rawHeaders;
    private final TBody body;

    /**
     * Create RestResponse.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param headers the deserialized headers of the HTTP response
     * @param rawHeaders the raw headers of the HTTP response
     * @param body the deserialized body
     */
    public RestResponse(HttpRequest request, int statusCode, THeaders headers, Map<String, String> rawHeaders, TBody body) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.rawHeaders = rawHeaders;
        this.body = body;
    }

    /**
     * @return the request which resulted in this RestResponse.
     */
    public HttpRequest request() {
        return request;
    }

    /**
     * @return the status code of the HTTP response.
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * @return the deserialized headers of the HTTP response.
     */
    public THeaders headers() {
        return headers;
    }

    /**
     * @return a Map containing the raw HTTP response headers.
     */
    public Map<String, String> rawHeaders() {
        return rawHeaders;
    }

    /**
     * @return the deserialized body of the HTTP response.
     */
    public TBody body() {
        return body;
    }
}
