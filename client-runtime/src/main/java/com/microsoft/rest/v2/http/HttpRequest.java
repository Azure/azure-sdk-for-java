/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.google.common.base.Charsets;

/**
 * This class contains all of the details necessary for sending a HTTP request through a HttpClient.
 */
public class HttpRequest {
    private final String callerMethod;
    private final String httpMethod;
    private final String url;
    private final HttpHeaders headers = new HttpHeaders();
    private HttpRequestBody body;

    /**
     * Create a new HttpRequest object with the provided HTTP method (GET, POST, PUT, etc.) and the
     * provided URL.
     * @param callerMethod The fully qualified method that was called to invoke this HTTP request.
     * @param httpMethod The HTTP method to use with this request.
     * @param url The URL where this HTTP request should be sent to.
     */
    public HttpRequest(String callerMethod, String httpMethod, String url) {
        this.callerMethod = callerMethod;
        this.httpMethod = httpMethod;
        this.url = url;
    }

    /**
     * Get the fully qualified method that was called to invoke this HTTP request.
     * @return The fully qualified method that was called to invoke this HTTP request.
     */
    public String callerMethod() {
        return callerMethod;
    }

    /**
     * Get the HTTP method that this request will use.
     * @return The HTTP method that this request will use.
     */
    public String httpMethod() {
        return httpMethod;
    }

    /**
     * Get the URL that this request will be sent to.
     * @return The URL that this request will be sent to.
     */
    public String url() {
        return url;
    }

    /**
     * Add the provided headerName and headerValue to the list of headers for this request.
     * @param headerName The name of the header.
     * @param headerValue The value of the header.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpRequest withHeader(String headerName, String headerValue) {
        headers.set(headerName, headerValue);
        return this;
    }

    /**
     * Get the headers for this request.
     * @return The headers for this request.
     */
    public HttpHeaders headers() {
        return headers;
    }

    /**
     * Set the body of this HTTP request.
     * @param body The body of this HTTP request.
     * @param mimeContentType The MIME Content-Type of the body's contents.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpRequest withBody(String body, String mimeContentType) {
        final byte[] bodyBytes = body.getBytes(Charsets.UTF_8);
        return withBody(bodyBytes, mimeContentType);
    }

    /**
     * Set the body of this HTTP request.
     * @param body The body of this HTTP request.
     * @param mimeContentType The MIME Content-Type of the body's contents.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpRequest withBody(byte[] body, String mimeContentType) {
        return withBody(new ByteArrayHttpRequestBody(body, mimeContentType));
    }

    /**
     * Set the body of this HTTP request.
     * @param body The body of this HTTP request.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpRequest withBody(HttpRequestBody body) {
        this.body = body;
        headers.set("Content-Length", String.valueOf(body.contentLength()));
        return this;
    }

    /**
     * Get the body for this HttpRequest.
     * @return The body for this HttpRequest.
     */
    public HttpRequestBody body() {
        return body;
    }

    /**
     * Get the assigned MIME type for this HttpRequest's body.
     * @return The assigned MIME type for this HttpRequest's body.
     */
    public String mimeContentType() {
        return body == null ? null : body.contentType();
    }
}
