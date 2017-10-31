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
    private String callerMethod;
    private String httpMethod;
    private String url;
    private HttpHeaders headers;
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
        this.headers = new HttpHeaders();
        this.body = null;
    }

    /**
     * Create a new HttpRequest object.
     * @param callerMethod The fully qualified method that was called to invoke this HTTP request.
     * @param httpMethod The HTTP method to use with this request.
     * @param url The URL where this HTTP request should be sent to.
     * @param headers The HTTP headers to use with this request.
     * @param body The body of this HTTP request.
     */
    public HttpRequest(String callerMethod, String httpMethod, String url, HttpHeaders headers, HttpRequestBody body) {
        this.callerMethod = callerMethod;
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Get the fully qualified method that was called to invoke this HTTP request.
     * @return The fully qualified method that was called to invoke this HTTP request.
     */
    public String callerMethod() {
        return callerMethod;
    }

    /**
     * Set the caller method for this request.
     * @param callerMethod The fully qualified method that was called to invoke this HTTP request.
     * @return This HttpRequest instance for chaining.
     */
    public HttpRequest withCallerMethod(String callerMethod) {
        this.callerMethod = callerMethod;
        return this;
    }

    /**
     * Get the HTTP method that this request will use.
     * @return The HTTP method that this request will use.
     */
    public String httpMethod() {
        return httpMethod;
    }

    /**
     * Set the HTTP method that this request will use.
     * @param httpMethod The HTTP method to use, e.g. "GET".
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpRequest withHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Get the URL that this request will be sent to.
     * @return The URL that this request will be sent to.
     */
    public String url() {
        return url;
    }

    /**
     * Set the URL that this request will be sent to.
     * @param url The new URL that this request will be sent to.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpRequest withUrl(String url) {
        this.url = url;
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
     * Set the headers for this request.
     * @param headers The set of headers to send for this request.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpRequest withHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
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
     * Get the body for this HttpRequest.
     * @return The body for this HttpRequest.
     */
    public HttpRequestBody body() {
        return body;
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
     * Performs a deep clone of this HTTP request.
     * @return A new HTTP request instance with cloned instances of all mutable properties.
     */
    public HttpRequest clone() {
        return new HttpRequest(callerMethod, httpMethod, url, new HttpHeaders(headers), body);
    }
}
