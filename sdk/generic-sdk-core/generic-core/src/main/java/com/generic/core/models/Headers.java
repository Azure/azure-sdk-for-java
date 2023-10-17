// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.http.HttpHeaderName;
import com.generic.core.util.CoreUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of headers on a request or response.
 */
public class Headers implements Iterable<Header> {
    // This map is a case-insensitive key (i.e. lower-cased), but the returned Header key will be as-provided to us
    private final Map<String, Header> headers;

    /**
     * Create an empty Headers instance.
     */
    public Headers() {
        headers = new HashMap<>();
    }

    /**
     * Create a Headers instance with the provided initial headers.
     *
     * @param headers the map of initial headers
     */
    public Headers(Map<String, String> headers) {
        this.headers = new HashMap<>(headers.size());
        headers.forEach((name, value) -> this.set(HttpHeaderName.fromString(name), value));
    }

    /**
     * Create a Headers instance with the provided initial headers.
     *
     * @param headers the collection of initial headers
     */
    public Headers(Iterable<Header> headers) {
        this.headers = new HashMap<>();
        for (final Header header : headers) {
            this.set(HttpHeaderName.fromString(header.getName()), header.getValuesList());
        }
    }

    Headers(Headers headers) {
        this.headers = new HashMap<>((int) (headers.headers.size() / 0.75f));
        headers.headers.forEach((key, value) ->
            this.headers.put(key, new Header(value.getName(), value.getValuesList())));
    }

    /**
     * Create a Headers instance with an initial {@code size} empty headers
     *
     * @param initialCapacity the initial capacity of headers map.
     */
    public Headers(int initialCapacity) {
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
     * Adds a {@link Header header} with the given name and value if a header with that name doesn't already exist,
     * otherwise adds the {@code value} to the existing header.
     *
     * @param name The name of the header.
     * @param value The value of the header.
     * @return The updated Headers object.
     */
    public Headers add(HttpHeaderName name, String value) {
        return addInternal(name.getCaseInsensitiveName(), name.getCaseSensitiveName(), value);
    }

    private Headers addInternal(String formattedName, String name, String value) {
        if (name == null || value == null) {
            return this;
        }

        headers.compute(formattedName, (key, header) -> {
            if (header == null) {
                return new Header(name, value);
            } else {
                header.addValue(value);
                return header;
            }
        });

        return this;
    }

    /**
     * Sets a {@link Header header} with the given name and value. If a header with same name already exists then
     * the value will be overwritten. If the given value is null, the header with the given name will be removed.
     *
     * @param name the name to set in the header. If it is null, this method will return with no changes to the
     * headers.
     * @param value the value
     * @return The updated Headers object
     */
    public Headers set(HttpHeaderName name, String value) {
        return setInternal(name.getCaseInsensitiveName(), name.getCaseSensitiveName(), value);
    }

    private Headers setInternal(String formattedName, String name, String value) {
        if (name == null) {
            return this;
        }

        if (value == null) {
            removeInternal(name);
        } else {
            headers.put(formattedName, new Header(name, value));
        }
        return this;
    }

    /**
     * Sets a {@link Header header} with the given name and the list of values provided, such that the given values
     * will be comma-separated when necessary. If a header with same name already exists then the values will be
     * overwritten. If the given values list is null, the header with the given name will be removed.
     *
     * @param name the name
     * @param values the values that will be comma-separated as appropriate
     * @return The updated Headers object
     */
    public Headers set(HttpHeaderName name, List<String> values) {
        return setInternal(name.getCaseInsensitiveName(), name.getCaseSensitiveName(), values);
    }

    private Headers setInternal(String formattedName, String name, List<String> values) {
        if (formattedName == null) {
            return this;
        }

        if (CoreUtils.isNullOrEmpty(values)) {
            removeInternal(formattedName);
        } else {
            headers.put(formattedName, new Header(name, values));
        }
        return this;
    }

    /**
     * Sets all provided header key/values pairs into this Headers instance. This is equivalent to calling {@code
     * headers.forEach(this::set)}, and therefore the behavior is as specified in {@link #set(HttpHeaderName, List)}. In
     * other words, this will create a header for each key in the provided map, replacing or removing an existing one,
     * depending on the value. If the given values list is null, the header with the given name will be removed. If the
     * given name is already a header, it will be removed and replaced with the headers provided.
     * <p>
     * Use {@link #setAllHeaders(Headers)} if you already have an instance of {@link Headers} as it provides better
     * performance.
     *
     * @param headers a map containing keys representing header names, and keys representing the associated values.
     * @return The updated Headers object
     * @throws NullPointerException If {@code headers} is null.
     */
    public Headers setAll(Map<String, List<String>> headers) {
        headers.forEach((name, value) -> setInternal(formatKey(name), name, value));
        return this;
    }

    /**
     * Sets all headers from the passed {@code headers} into this {@link Headers}.
     * <p>
     * This is the equivalent to calling {@code headers.forEach(header -> set(header.getName(), header.getValuesList())}
     * and therefore the behavior is as specified in {@link #set(HttpHeaderName, List)}.
     * <p>
     * If {@code headers} is null this is a no-op.
     *
     * @param headers The headers to add into this {@link Headers}.
     * @return The updated Headers object.
     */
    public Headers setAllHeaders(Headers headers) {
        if (headers != null) {
            headers.headers.forEach((headerName, header) ->
                setInternal(headerName, header.getName(), header.getValuesList()));
        }

        return this;
    }

    /**
     * Gets the {@link Header header} for the provided header name. null is returned if the header isn't
     * found.
     *
     * @param name the name of the header to find.
     * @return the header if found, null otherwise.
     */
    public Header get(HttpHeaderName name) {
        return getInternal(name.getCaseInsensitiveName());
    }

    private Header getInternal(String formattedName) {
        return headers.get(formattedName);
    }

    /**
     * Removes the {@link Header header} with the provided header name. null is returned if the header isn't
     * found.
     *
     * @param name the name of the header to remove.
     * @return the header if removed, null otherwise.
     */
    public Header remove(HttpHeaderName name) {
        return removeInternal(name.getCaseInsensitiveName());
    }

    private Header removeInternal(String formattedName) {
        return headers.remove(formattedName);
    }

    /**
     * Get the value for the provided header name. null is returned if the header name isn't found.
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the value of the header, or null if the header isn't found
     */
    public String getValue(HttpHeaderName name) {
        return getValueInternal(name.getCaseInsensitiveName());
    }

    private String getValueInternal(String formattedName) {
        final Header header = getInternal(formattedName);
        return header == null ? null : header.getValue();
    }

    /**
     * Get the values for the provided header name. null is returned if the header name isn't found.
     *
     * <p>This returns {@link #getValue(HttpHeaderName) getValue} split by {@code comma}.</p>
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the values of the header, or null if the header isn't found
     */
    public String[] getValues(HttpHeaderName name) {
        return getValuesInternal(name.getCaseInsensitiveName());
    }

    private String[] getValuesInternal(String formattedName) {
        final Header header = getInternal(formattedName);
        return header == null ? null : header.getValues();
    }

    /**
     * Returns a copy of the headers as an unmodifiable {@link Map} representation of the state of the headers at
     * the time of the toMap call. This map will not change as the underlying headers change, and nor will
     * modifying the key or values contained in the map have any effect on the state of the headers.
     *
     * <p>Note that there may be performance implications of using Map APIs on the returned Map. It is highly
     * recommended that users prefer to use alternate APIs present on the Headers class, over using APIs present on
     * the returned Map class. For example, use the {@link #get(HttpHeaderName)} API, rather than {@code headers.toMap().get
     * (name)}.</p>
     *
     * @return the headers in a copied and unmodifiable form.
     */
    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();
        for (final Header header : headers.values()) {
            result.put(header.getName(), header.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns a copy of the headers as an unmodifiable {@link Map} representation of the state of the headers at
     * the time of the toMultiMap call. This map will not change as the underlying headers change, and nor will
     * modifying the key or values contained in the map have any effect on the state of the headers.
     *
     * <p>Note that there may be performance implications of using Map APIs on the returned Map. It is highly
     * recommended that users prefer to use alternate APIs present on the Headers class, over using APIs present on
     * the returned Map class. For example, use the {@link #get(HttpHeaderName)} API, rather than {@code headers.toMap()
     * .get(name)}.</p>
     *
     * @return the headers in a copied and unmodifiable form.
     */
    Map<String, String[]> toMultiMap() {
        final Map<String, String[]> result = new HashMap<>();
        for (final Header header : headers.values()) {
            result.put(header.getName(), header.getValues());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Header> iterator() {
        return headers.values().iterator();
    }

    /**
     * Get a {@link Stream} representation of the Header values in this instance.
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

    private static String formatKey(String name) {
        return (name == null) ? null : name.toLowerCase(Locale.ROOT);
    }
}
