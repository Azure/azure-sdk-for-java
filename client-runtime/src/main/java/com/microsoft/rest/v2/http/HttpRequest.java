/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all of the details necessary for sending a HTTP request through a HttpClient.
 */
public class HttpRequest {
    private final String method;
    private final String url;
    private final Map<String, String> headers = new HashMap<>();
    private String body;
    private String mimeType;

    /**
     * Create a new HttpRequest object with the provided HTTP method (GET, POST, PUT, etc.) and the
     * provided URL.
     * @param method The HTTP method to use with this request.
     * @param url The URL where this HTTP request should be sent to.
     */
    public HttpRequest(String method, String url) {
        this.method = method;
        this.url = url;
    }

    /**
     * Get the HTTP method that this request will use.
     * @return The HTTP method that this request will use.
     */
    public String method() {
        return method;
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
        if (!headers.containsKey(headerName)) {
            headers.put(headerName, headerValue);
        }
        else {
            headers.put(headerName, headers.get(headerName) + "," + headerValue);
        }
        return this;
    }

    /**
     * Set the body of this HTTP request.
     * @param body The body of this HTTP request.
     * @param mimeType The MIME type of the body's contents.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpRequest withBody(String body, String mimeType) {
        this.body = body;
        this.mimeType = mimeType;
        return this;
    }

    /**
     * Get the body for this HttpRequest.
     * @return The body for this HttpRequest.
     */
    public String body() {
        return body;
    }

    /**
     * Get the assigned MIME type for this HttpRequest's body.
     * @return The assigned MIME type for this HttpRequest's body.
     */
    public String mimeType() {
        return mimeType;
    }
}
