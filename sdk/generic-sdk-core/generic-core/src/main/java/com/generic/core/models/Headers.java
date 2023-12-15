// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.implementation.util.CoreUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of {@link Headers} on a request or response.
 */
public class Headers implements Iterable<Header> {
    /**
     * This map is a case-insensitive key (i.e. lower-cased), but the returned {@link Header} key will be as-provided to
     * us.
     */
    private final Map<String, Header> headers;

    /**
     * Create an empty {@link Headers} instance.
     */
    public Headers() {
        headers = new HashMap<>();
    }

    /**
     * Create a {@link Headers} instance with the provided initial {@link Headers}.
     *
     * @param headers The map of initial {@link Headers}.
     */
    public Headers(Map<String, String> headers) {
        this.headers = new HashMap<>(headers.size());
        headers.forEach((name, value) -> this.set(HeaderName.fromString(name), value));
    }

    /**
     * Create a {@link Headers} instance with the provided initial {@link Headers}.
     *
     * @param headers The collection of initial {@link Headers}.
     */
    public Headers(Iterable<Header> headers) {
        this.headers = new HashMap<>();
        for (final Header header : headers) {
            this.set(HeaderName.fromString(header.getName()), header.getValuesList());
        }
    }

    Headers(Headers headers) {
        this.headers = new HashMap<>((int) (headers.headers.size() / 0.75f));

        headers.headers.forEach((key, value) ->
            this.headers.put(key, new Header(value.getName(), value.getValuesList())));
    }

    /**
     * Create a {@link Headers} instance with an initial {@code size} empty headers.
     *
     * @param initialCapacity The initial capacity of {@link Headers} map.
     */
    public Headers(int initialCapacity) {
        this.headers = new HashMap<>(initialCapacity);
    }

    /**
     * Gets the number of {@link Headers} in the collection.
     *
     * @return The number of {@link Headers} in this collection.
     */
    public int getSize() {
        return headers.size();
    }

    /**
     * Adds a {@link Header} with the given name and value if a {@link Header} with that name doesn't already exist,
     * otherwise adds the {@code value} to the existing header.
     *
     * @param name The name of the {@link Header}.
     * @param value The value of the {@link Header}.
     *
     * @return The updated {@link Headers} object.
     */
    public Headers add(HeaderName name, String value) {
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
     * Sets a {@link Header} with the given name and value. If a {@link Header} with same name already exists then the
     * value will be overwritten. If the given value is {@code null}, the header with the given name will be removed.
     *
     * @param name The name to set in the {@link Header}. If it is {@code null}, this method will return with no changes
     * made to the {@link Headers}.
     * @param value The value of the {@link Header}.
     *
     * @return The updated {@link Headers} object.
     */
    public Headers set(HeaderName name, String value) {
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
     * Sets a {@link Header} with the given name and the list of values provided, such that the given values will be
     * comma-separated when necessary. If a {@link Header} with same name already exists then the values will be
     * overwritten. If the given values list is {@code null}, the {@link Header} with the given name will be removed.
     *
     * @param name The {@link Header} name.
     * @param values The values that will be comma-separated as appropriate.
     *
     * @return The updated {@link Headers} object.
     */
    public Headers set(HeaderName name, List<String> values) {
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
     * Sets all provided {@link Header} key/values pairs into this {@link Headers} instance. This is equivalent to
     * calling {@code headers.forEach(this::set)}, and therefore the behavior is as specified in
     * {@link #set(HeaderName, List)}. In other words, this will create a {@link Header} for each key in the
     * provided map, replacing or removing an existing one, depending on the value. If the given values list is
     * {@code null}, the header with the given name will be removed. If the given name is already a {@link Header}, it
     * will be removed and replaced with the provided {@link Headers}.
     *
     * <p>Use {@link #setAllHeaders(Headers)} if you already have an instance of {@link Headers} as it provides better
     * performance.</p>
     *
     * @param headers A map containing keys representing {@link Header} names, and keys representing the associated
     * values.
     *
     * @return The updated {@link Headers} object.
     *
     * @throws NullPointerException If {@code headers} is {@code null}.
     */
    public Headers setAll(Map<String, List<String>> headers) {
        headers.forEach((name, value) -> setInternal(formatKey(name), name, value));

        return this;
    }

    /**
     * Sets all the provided {@link Headers} into this {@link Headers} instance.
     *
     * <p>This is the equivalent to calling
     * {@code headers.forEach(header -> set(header.getName(), header.getValuesList())} and therefore the behavior is as
     * specified in {@link #set(HeaderName, List)}.</p>
     *
     * <p>If {@code headers} is {@code null} this is a no-op.</p>
     *
     * @param headers The headers to add into this {@link Headers}.
     *
     * @return The updated {@link Headers} object.
     */
    public Headers setAllHeaders(Headers headers) {
        if (headers != null) {
            headers.headers.forEach((headerName, header) ->
                setInternal(headerName, header.getName(), header.getValuesList()));
        }

        return this;
    }

    /**
     * Gets the {@link Header} for the provided header name. {@code null} is returned if the {@link Header} isn't found.
     *
     * @param name The name of the {@link Header} to find.
     *
     * @return The {@link Header} if found, {@code null} otherwise.
     */
    public Header get(HeaderName name) {
        return getInternal(name.getCaseInsensitiveName());
    }

    private Header getInternal(String formattedName) {
        return headers.get(formattedName);
    }

    /**
     * Removes the {@link Header} with the provided header name. {@code null} is returned if the {@link Header} isn't
     * found.
     *
     * @param name The name of the {@link Header} to remove.
     *
     * @return The {@link Header} if removed, {@code null} otherwise.
     */
    public Header remove(HeaderName name) {
        return removeInternal(name.getCaseInsensitiveName());
    }

    private Header removeInternal(String formattedName) {
        return headers.remove(formattedName);
    }

    /**
     * Get the value for the provided {@link Header} name. {@code null} is returned if the {@link Header} name isn't
     * found.
     *
     * @param name The name of the {@link Header} whose value is being retrieved.
     *
     * @return The value of the {@link Header}, or {@code null} if the {@link Header} isn't found.
     */
    public String getValue(HeaderName name) {
        return getValueInternal(name.getCaseInsensitiveName());
    }

    private String getValueInternal(String formattedName) {
        final Header header = getInternal(formattedName);

        return header == null ? null : header.getValue();
    }

    /**
     * Get the values for the provided {@link Header} name. {@code null} is returned if the {@link Header} name isn't
     * found.
     *
     * <p>This returns {@link #getValue(HeaderName) getValue} split by {@code comma}.</p>
     *
     * @param name The name of the {@link Header} whose value is being retrieved.
     *
     * @return The values of the {@link Header}, or {@code null} if the {@link Header} isn't found.
     */
    public String[] getValues(HeaderName name) {
        return getValuesInternal(name.getCaseInsensitiveName());
    }

    private String[] getValuesInternal(String formattedName) {
        final Header header = getInternal(formattedName);

        return header == null ? null : header.getValues();
    }

    /**
     * Returns a copy of the {@link Headers} as an unmodifiable {@link Map} representation of the state of the
     * {@link Headers} at the time of the {@code toMap} call. This map will not change as the underlying {@link Headers}
     * change, and nor will modifying the key or values contained in the map have any effect on the state of the
     * {@link Headers}.
     *
     * <p>Note that there may be performance implications of using {@link Map} APIs on the returned {@link Map}. It is
     * highly recommended that users prefer to use alternate APIs present on the {@link Headers} class, over using APIs
     * present on the returned {@link Map} class. For example, use the {@link #get(HeaderName)} API, rather than
     * {@code headers.toMap().get(name)}.</p>
     *
     * @return The {@link Headers} in a copied and unmodifiable form.
     */
    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();

        for (final Header header : headers.values()) {
            result.put(header.getName(), header.getValue());
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns a copy of the {@link Headers} as an unmodifiable {@link Map} representation of the state of the
     * {@link Headers} at the time of the {@code toMultiMap} call. This {@link Map} will not change as the underlying
     * {@link Headers} change, and nor will modifying the key or values contained in the {@link Map} have any effect on
     * the state of the {@link Headers}.
     *
     * <p>Note that there may be performance implications of using {@link Map} APIs on the returned {@link Map}. It is
     * highly recommended that users prefer to use alternate APIs present on the {@link Headers} class, over using APIs
     * present on the returned {@link Map} class. For example, use the {@link #get(HeaderName)} API, rather than
     * {@code headers.toMap().get(name)}.</p>
     *
     * @return The {@link Headers} in a copied and unmodifiable form.
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
     * Get a {@link Stream} representation of the {@link Header} values in this instance.
     *
     * @return A {@link Stream} of all {@link Header} values in this instance.
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
