/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of headers that will be applied to a HTTP request.
 */
public class HttpHeaders implements Iterable<HttpHeader> {
    private final Map<String, HttpHeader> headers = new HashMap<>();

    /**
     * Create an empty HttpHeaders object.
     */
    public HttpHeaders() {
    }

    /**
     * Create a HttpHeaders object with the provided initial headers.
     * @param headers The map of name to value associations to use as initial headers.
     */
    public HttpHeaders(Map<String, String> headers) {
        for (final Map.Entry<String, String> header : headers.entrySet()) {
            this.add(header.getKey(), header.getValue());
        }
    }

    /**
     * Create a HttpHeaders object with the provided initial headers.
     * @param headers The map of name to value associations to use as initial headers.
     */
    public HttpHeaders(Iterable<HttpHeader> headers) {
        this();

        for (final HttpHeader header : headers) {
            this.add(header.name(), header.value());
        }
    }

    /**
     * Add the provided headerName and headerValue to the list of headers for this request.
     * @param headerName The name of the header.
     * @param headerValue The value of the header.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpHeaders add(String headerName, String headerValue) {
        final String headerKey = headerName.toLowerCase();
        if (!headers.containsKey(headerKey)) {
            headers.put(headerKey, new HttpHeader(headerName, headerValue));
        }
        else {
            headers.get(headerKey).addValue(headerValue);
        }
        return this;
    }

    /**
     * Get the header value for the provided header name. If the header name isn't found, then null
     * will be returned.
     * @param headerName The name of the header to look for.
     * @return The String value of the header, or null if the header isn't found.
     */
    public String get(String headerName) {
        final String headerKey = headerName.toLowerCase();
        final HttpHeader header = headers.get(headerKey);
        return header == null ? null : header.value();
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        return headers.values().iterator();
    }
}
