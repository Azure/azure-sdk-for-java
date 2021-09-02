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
    // This map is a case-insensitive key (i.e. lower-cased), but the returned HttpHeader key will be as-provided to us
    private final Map<String, HttpHeader> headers;

    /**
     * Create an empty HttpHeaders instance.
     */
    public HttpHeaders() {
        headers = new HashMap<>();
    }

    /**
     * Create a HttpHeaders instance with the provided initial headers.
     *
     * @param headers the map of initial headers
     */
    public HttpHeaders(Map<String, String> headers) {
        this.headers = new HashMap<>(headers.size());
        headers.forEach(this::set);
    }

    /**
     * Create a HttpHeaders instance with the provided initial headers.
     *
     * @param headers the collection of initial headers
     */
    public HttpHeaders(Iterable<HttpHeader> headers) {
        this.headers = new HashMap<>();
        for (final HttpHeader header : headers) {
            this.set(header.getName(), header.getValue());
        }
    }

    /**
     * Create a HttpHeaders instance with an initial {@code size} empty headers
     *
     * @param initialCapacity the initial capacity of headers map.
     */
    public HttpHeaders(int initialCapacity) {
        this.headers = new HashMap<>(initialCapacity);
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
     * Adds a {@link HttpHeader header} with the given name and value if a header with that name doesn't already exist,
     * otherwise adds the {@code value} to the existing header.
     *
     * @param name The name of the header.
     * @param value The value of the header.
     * @return The updated HttpHeaders object.
     */
    public HttpHeaders add(String name, String value) {
        if (name == null) {
            return this;
        }
        String caseInsensitiveName = formatKey(name);
        if (value == null) {
            return this;
        } else {
            headers.compute(caseInsensitiveName, (key, header) -> {
                if (header == null) {
                    return new HttpHeader(name, value);
                } else {
                    header.addValue(value);
                    return header;
                }
            });
        }
        return this;
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

    /**
     * Sets a {@link HttpHeader header} with the given name and value. If a header with same name already exists then
     * the value will be overwritten. If the given value is null, the header with the given name will be removed.
     *
     * @param name the name to set in the header. If it is null, this method will return with no changes to the
     * headers.
     * @param value the value
     * @return The updated HttpHeaders object
     */
    public HttpHeaders set(String name, String value) {
        if (name == null) {
            return this;
        }
        String caseInsensitiveName = formatKey(name);
        if (value == null) {
            remove(caseInsensitiveName);
        } else {
            headers.put(caseInsensitiveName, new HttpHeader(name, value));
        }
        return this;
    }

    /**
     * Sets a {@link HttpHeader header} with the given name and the list of values provided, such that the given values
     * will be comma-separated when necessary. If a header with same name already exists then the values will be
     * overwritten. If the given values list is null, the header with the given name will be removed.
     *
     * @param name the name
     * @param values the values that will be comma-separated as appropriate
     * @return The updated HttpHeaders object
     */
    public HttpHeaders set(String name, List<String> values) {
        if (name == null) {
            return this;
        }
        String caseInsensitiveName = formatKey(name);
        if (values == null) {
            remove(caseInsensitiveName);
        } else {
            headers.put(caseInsensitiveName, new HttpHeader(name, values));
        }
        return this;
    }

    /**
     * Sets all provided header key/values pairs into this HttpHeaders instance. This is equivalent to calling {@code
     * headers.forEach(this::set)}, and therefore the behavior is as specified in {@link #set(String, List)}. In other
     * words, this will create a header for each key in the provided map, replacing or removing an existing one,
     * depending on the value. If the given values list is null, the header with the given name will be removed. If the
     * given name is already a header, it will be removed and replaced with the headers provided.
     *
     * @param headers a map containing keys representing header names, and keys representing the associated values.
     * @return The updated HttpHeaders object
     */
    public HttpHeaders setAll(Map<String, List<String>> headers) {
        headers.forEach(this::set);
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
     * Removes the {@link HttpHeader header} with the provided header name. {@code Null} is returned if the header isn't
     * found.
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
     * Returns a copy of the http headers as an unmodifiable {@link Map} representation of the state of the headers at
     * the time of the toMap call. This map will not change as the underlying http headers change, and nor will
     * modifying the key or values contained in the map have any effect on the state of the http headers.
     *
     * <p>Note that there may be performance implications of using Map APIs on the returned Map. It is highly
     * recommended that users prefer to use alternate APIs present on the HttpHeaders class, over using APIs present on
     * the returned Map class. For example, use the {@link #get(String)} API, rather than {@code
     * httpHeaders.toMap().get(name)}.</p>
     *
     * @return the headers in a copied and unmodifiable form.
     */
    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers.values()) {
            result.put(header.getName(), header.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns a copy of the http headers as an unmodifiable {@link Map} representation of the state of the headers at
     * the time of the toMultiMap call. This map will not change as the underlying http headers change, and nor will
     * modifying the key or values contained in the map have any effect on the state of the http headers.
     *
     * <p>Note that there may be performance implications of using Map APIs on the returned Map. It is highly
     * recommended that users prefer to use alternate APIs present on the HttpHeaders class, over using APIs present on
     * the returned Map class. For example, use the {@link #get(String)} API, rather than {@code
     * httpHeaders.toMap().get(name)}.</p>
     *
     * @return the headers in a copied and unmodifiable form.
     */
    public Map<String, String[]> toMultiMap() {
        final Map<String, String[]> result = new HashMap<>();
        for (final HttpHeader header : headers.values()) {
            result.put(header.getName(), header.getValues());
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
