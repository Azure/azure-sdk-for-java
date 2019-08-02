// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * A collection of headers on an HTTP request or response.
 */
public class HttpHeaders implements Iterable<HttpHeader> {
    private final Map<String, HttpHeader> headers = new HashMap<>();

    /**
     * Create an empty HttpHeaders instance.
     */
    public HttpHeaders() {
    }

    /**
     * Create a HttpHeaders instance with the provided initial headers.
     *
     * @param headers the map of initial headers
     */
    public HttpHeaders(Map<String, String> headers) {
        for (final Map.Entry<String, String> header : headers.entrySet()) {
            this.put(header.getKey(), header.getValue());
        }
    }

    /**
     * Create a HttpHeaders instance with the provided initial headers.
     *
     * @param headers the collection of initial headers
     */
    public HttpHeaders(Iterable<HttpHeader> headers) {
        this();

        for (final HttpHeader header : headers) {
            this.put(header.name(), header.value());
        }
    }

    /**
     * Gets the number of headers in the collection.
     *
     * @return the number of headers in this collection.
     */
    public int size() {
        return headers.size();
    }

    /**
     * Set a header.
     *
     * If header with same name already exists then the value will be overwritten.
     *
     * @param name the name
     * @param value the value
     * @return The updated HttpHeaders object
     */
    public HttpHeaders put(String name, String value) {
        headers.put(formatKey(name), new HttpHeader(name, value));
        return this;
    }

    /**
     * Get the {@link HttpHeader header} for the provided header name. Null will be returned if the header isn't found.
     *
     * @param name the name of the header to find.
     * @return the header if found, null otherwise.
     */
    public HttpHeader get(String name) {
        return headers.get(formatKey(name));
    }

    /**
     * Get the header value for the provided header name. Null will be returned if the header
     * name isn't found.
     *
     * @param name the name of the header to look for
     * @return The String value of the header, or null if the header isn't found
     */
    public String value(String name) {
        final HttpHeader header = get(name);
        return header == null ? null : header.value();
    }

    /**
     * Get the header values for the provided header name. Null will be returned if
     * the header name isn't found.
     *
     * @param name the name of the header to look for
     * @return the values of the header, or null if the header isn't found
     */
    public String[] values(String name) {
        final HttpHeader header = get(name);
        return header == null ? null : header.values();
    }

    private String formatKey(final String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    /**
     * Get {@link Map} representation of the HttpHeaders collection.
     *
     * @return the headers as map
     */
    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers.values()) {
            result.put(header.name(), header.value());
        }
        return result;
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        return headers.values().iterator();
    }
}
