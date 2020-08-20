// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link Iterable} map representation for {@link Header}.
 */
public class Headers implements Iterable<Header> {
    private final Map<String, Header> headers = new ConcurrentHashMap<>();

    /**
     * Create an empty Headers instance.
     */
    public Headers() {
    }

    /**
     * Create a Headers instance with the provided initial headers.
     *
     * @param headers the map of initial headers
     */
    public Headers(Map<String, String> headers) {
        for (final Map.Entry<String, String> header : headers.entrySet()) {
            this.put(header.getKey(), header.getValue());
        }
    }

    /**
     * Create a Headers instance with the provided initial headers.
     *
     * @param headers the collection of initial headers
     */
    public Headers(Iterable<Header> headers) {
        this();

        for (final Header header : headers) {
            this.put(header.getName(), header.getValue());
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
     * Sets a {@link Header header} with the given name and value.
     *
     * <p>If header with same name already exists then the value will be overwritten.</p>
     *
     * @param name the name
     * @param value the value
     * @return The updated HttpHeaders object
     */
    public Headers put(String name, String value) {
        headers.put(formatKey(name), new Header(name, value));
        return this;
    }

    /**
     * Gets the {@link Header header} for the provided header name. {@code Null} is returned if the header isn't
     * found.
     *
     * @param name the name of the header to find.
     * @return the header if found, null otherwise.
     */
    public Header get(String name) {
        return headers.get(formatKey(name));
    }

    /**
     * Removes the {@link Header header} with the provided header name. {@code Null} is returned if the header
     * isn't found.
     *
     * @param name the name of the header to remove.
     * @return the header if removed, null otherwise.
     */
    public Header remove(String name) {
        return headers.remove(formatKey(name));
    }

    /**
     * Get the value for the provided header name. {@code Null} is returned if the header name isn't found.
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the value of the header, or null if the header isn't found
     */
    public String getValue(String name) {
        final Header header = get(name);
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
        final Header header = get(name);
        return header == null ? null : header.getValues();
    }

    private String formatKey(final String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    /**
     * Gets a {@link Map} representation of the HttpHeaders collection.
     *
     * @return the headers as map
     */
    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();
        for (final Header header : headers.values()) {
            result.put(header.getName(), header.getValue());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Header> iterator() {
        return headers.values().iterator();
    }

    /**
     * Get a {@link Stream} representation of the HttpHeader values in this instance.
     *
     * @return A {@link Stream} of all header values in this instance.
     */
    public Stream<Header> stream() {
        return headers.values().stream();
    }

    @Override
    public String toString() {
        return this.stream()
            .map(header -> header.getName() + "=" + header.getValue())
            .collect(Collectors.joining(", "));
    }

}
