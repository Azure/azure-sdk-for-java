// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        headers.forEach(this::set);
    }

    /**
     * Create a HttpHeaders instance with the provided initial headers.
     *
     * @param headers the collection of initial headers
     */
    public HttpHeaders(Iterable<HttpHeader> headers) {
        for (final HttpHeader header : headers) {
            this.set(header.getName(), header.getValue());
        }
    }

    /**
     * Gets the number of headers in the collection.
     *
     * @return the number of headers in this collection.
     */
    public int getSize() {
        return headers.size();
    }

    /**
     * Sets a {@link HttpHeader header} with the given name and value.
     *
     * <p>If header with same name already exists then the value will be overwritten.</p>
     *
     * @param name the name
     * @param value the value
     * @return The updated HttpHeaders object
     * @deprecated Use {@link #set(String, String)} instead.
     */
    @Deprecated
    public HttpHeaders put(String name, String value) {
        return set(name, value);
    }

    public HttpHeaders set(String name, String value) {
        headers.put(formatKey(name), new HttpHeader(name, value));
        return this;
    }

    public HttpHeaders set(String name, List<String> values) {
        headers.put(formatKey(name), new HttpHeader(name, values));
        return this;
    }

    /**
     * Gets the {@link HttpHeader header} for the provided header name. {@code Null} is returned if the header isn't
     * found.
     *
     * @param name the name of the header to find.
     * @return the header if found, null otherwise.
     */
    public HttpHeader get(String name) {
        return headers.get(formatKey(name));
    }

    /**
     * Removes the {@link HttpHeader header} with the provided header name. {@code Null} is returned if the header
     * isn't found.
     *
     * @param name the name of the header to remove.
     * @return the header if removed, null otherwise.
     */
    public HttpHeader remove(String name) {
        return headers.remove(formatKey(name));
    }

    /**
     * Get the value for the provided header name. {@code Null} is returned if the header name isn't found.
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the value of the header, or null if the header isn't found
     */
    public String getValue(String name) {
        final HttpHeader header = get(name);
        return header == null ? null : header.getValue();
    }

    /**
     * Get the values for the provided header name. {@code Null} is returned if the header name isn't found.
     *
     * <p>This returns {@link #getValue(String) getValue} split by {@code comma}.</p>
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the values of the header, or null if the header isn't found
     */
    public String[] getValues(String name) {
        final HttpHeader header = get(name);
        return header == null ? null : header.getValues();
    }

    private String formatKey(final String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    /**
     * Gets an unmodifiable {@link Map} representation of the HttpHeaders collection.
     *
     * @return the headers as map in an unmodifiable form.
     */
    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers.values()) {
            result.put(header.getName(), header.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<HttpHeader> iterator() {
        return headers.values().iterator();
    }

    /**
     * Get a {@link Stream} representation of the HttpHeader values in this instance.
     *
     * @return A {@link Stream} of all header values in this instance.
     */
    public Stream<HttpHeader> stream() {
        return headers.values().stream();
    }

    @Override
    public String toString() {
        return this.stream()
            .map(header -> header.getName() + "=" + header.getValue())
            .collect(Collectors.joining(", "));
    }
}
