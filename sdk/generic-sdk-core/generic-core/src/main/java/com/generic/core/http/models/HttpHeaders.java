// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.implementation.util.CoreUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of {@link HttpHeaders} on a request or response.
 */
public class HttpHeaders implements Iterable<HttpHeader> {
    /**
     * This map is a case-insensitive key (i.e. lower-cased), but the returned {@link HttpHeader} key will be as-provided to
     * us.
     */
    private final Map<HttpHeaderName, HttpHeader> headers;

    /**
     * Create an empty {@link HttpHeaders} instance.
     */
    public HttpHeaders() {
        headers = new HashMap<>();
    }

    /**
     * Create a {@link HttpHeaders} instance with the provided initial {@link HttpHeaders}.
     *
     * @param headers The map of initial {@link HttpHeaders}.
     */
    public HttpHeaders(Map<HttpHeaderName, HttpHeader> headers) {
        this.headers = new HashMap<>(headers.size());
        headers.forEach((name, value) -> this.set(name, value.getValue()));
    }

    /**
     * Create a {@link HttpHeaders} instance with the provided initial {@link HttpHeaders}.
     * <p>
     * This constructor is a deep copy of the provided {@link HttpHeaders}.
     *
     * @param headers The initial {@link HttpHeaders} to copy.
     */
    public HttpHeaders(HttpHeaders headers) {
        this.headers = new HashMap<>((int) (headers.headers.size() / 0.75f));

        headers.headers.forEach((key, value) ->
            this.headers.put(key, new HttpHeader(value.getName(), value.getValues())));
    }

    /**
     * Create a {@link HttpHeaders} instance with an initial {@code size} empty headers.
     *
     * @param initialCapacity The initial capacity of {@link HttpHeaders} map.
     */
    public HttpHeaders(int initialCapacity) {
        this.headers = new HashMap<>(initialCapacity);
    }

    /**
     * Gets the number of {@link HttpHeaders} in the collection.
     *
     * @return The number of {@link HttpHeaders} in this collection.
     */
    public int getSize() {
        return headers.size();
    }

    /**
     * Adds a {@link HttpHeader} with the given name and value if a {@link HttpHeader} with
     * that name doesn't already exist,
     * @param header The {@link HttpHeader} to add.
     * @return The updated {@link HttpHeaders} object.
     */
    public HttpHeaders add(HttpHeader header) {
        return addInternal(HttpHeaderName.fromString(header.getName().getCaseInsensitiveName()),
            HttpHeaderName.fromString(header.getName().getCaseSensitiveName()), header.getValues());
    }

    /**
     * Adds a {@link HttpHeader} with the given name and value if a {@link HttpHeader} with that name doesn't already exist,
     * otherwise adds the {@code value} to the existing header.
     *
     * @param name The name of the {@link HttpHeader}.
     * @param value The value of the {@link HttpHeader}.
     *
     * @return The updated {@link HttpHeaders} object.
     */
    public HttpHeaders add(HttpHeaderName name, String value) {
        return addInternal(HttpHeaderName.fromString(name.getCaseInsensitiveName()),
            HttpHeaderName.fromString(name.getCaseSensitiveName()), value);
    }

    /**
     * Adds a {@link HttpHeader} with the given name and value if a {@link HttpHeader} with that name doesn't already exist,
     * otherwise adds the {@code values} to the existing header.
     *
     * @param name The name of the {@link HttpHeader}.
     * @param values The values of the {@link HttpHeader}.
     * @return The updated {@link HttpHeaders} object.
     */
    public HttpHeaders add(HttpHeaderName name, List<String> values) {
        return addInternal(HttpHeaderName.fromString(name.getCaseInsensitiveName()),
            HttpHeaderName.fromString(name.getCaseSensitiveName()), values);
    }

    private HttpHeaders addInternal(HttpHeaderName formattedName, HttpHeaderName name, String value) {
        if (name == null || value == null) {
            return this;
        }

        headers.compute(formattedName, (key, header) -> {
            if (header == null) {
                return new HttpHeader(name, value);
            } else {
                header.addValue(value);
                return header;
            }
        });

        return this;
    }

    /**
     * Adds all the provided {@link HttpHeaders} into this {@link HttpHeaders} instance.
     *
     * <p>This is the equivalent to calling
     * {@code headers.forEach(header -> add(header.getName(), header.getValuesList())} and therefore the behavior is as
     * specified in {@link #add(HttpHeaderName, List)}.</p>
     *
     * <p>If {@code headers} is {@code null} this is a no-op.</p>
     *
     * @param headers The headers to add into this {@link HttpHeaders}.
     *
     * @return The updated {@link HttpHeaders} object.
     */
    public HttpHeaders addAllHeaders(HttpHeaders headers) {
        if (headers != null) {
            headers.headers.forEach((headerName, header) ->
                addInternal(HttpHeaderName.fromString(headerName.getCaseInsensitiveName()),
                    HttpHeaderName.fromString(header.getName().getCaseSensitiveName()),
                    header.getValues()));
        }

        return this;
    }

    private HttpHeaders addInternal(HttpHeaderName formattedName, HttpHeaderName name, List<String> values) {
        if (name == null || CoreUtils.isNullOrEmpty(values)) {
            return this;
        }

        headers.compute(formattedName, (key, header) -> {
            if (header == null) {
                return new HttpHeader(name, values);
            } else {
                header.addValues(values);
                return header;
            }
        });

        return this;
    }

    /**
     * Sets a {@link HttpHeader} with the given name and value. If a {@link HttpHeader} with same name already exists then the
     * value will be overwritten. If the given value is {@code null}, the header with the given name will be removed.
     *
     * @param name The name to set in the {@link HttpHeader}. If it is {@code null}, this method will return with no changes
     * made to the {@link HttpHeaders}.
     * @param value The value of the {@link HttpHeader}.
     *
     * @return The updated {@link HttpHeaders} object.
     */
    public HttpHeaders set(HttpHeaderName name, String value) {
        return setInternal(name.getCaseInsensitiveName(), name.getCaseSensitiveName(), value);
    }

    private HttpHeaders setInternal(String formattedName, String name, String value) {
        if (name == null) {
            return this;
        }

        if (value == null) {
            removeInternal(name);
        } else {
            headers.put(HttpHeaderName.fromString(formattedName), new HttpHeader(HttpHeaderName.fromString(name), value));
        }

        return this;
    }

    /**
     * Sets a {@link HttpHeader} with the given name and the list of values provided, such that the given values will be
     * comma-separated when necessary. If a {@link HttpHeader} with same name already exists then the values will be
     * overwritten. If the given values list is {@code null}, the {@link HttpHeader} with the given name will be removed.
     *
     * @param name The {@link HttpHeader} name.
     * @param values The values that will be comma-separated as appropriate.
     *
     * @return The updated {@link HttpHeaders} object.
     */
    public HttpHeaders set(HttpHeaderName name, List<String> values) {
        return setInternal(name.getCaseInsensitiveName(), name.getCaseSensitiveName(), values);
    }

    private HttpHeaders setInternal(String formattedName, String name, List<String> values) {
        if (formattedName == null) {
            return this;
        }

        if (CoreUtils.isNullOrEmpty(values)) {
            removeInternal(formattedName);
        } else {
            headers.put(HttpHeaderName.fromString(formattedName), new HttpHeader(HttpHeaderName.fromString(name), values));
        }

        return this;
    }

    /**
     * Sets all the provided {@link HttpHeaders} into this {@link HttpHeaders} instance.
     *
     * <p>This is the equivalent to calling
     * {@code headers.forEach(header -> set(header.getName(), header.getValuesList())} and therefore the behavior is as
     * specified in {@link #set(HttpHeaderName, List)}.</p>
     *
     * <p>If {@code headers} is {@code null} this is a no-op.</p>
     *
     * @param headers The headers to add into this {@link HttpHeaders}.
     *
     * @return The updated {@link HttpHeaders} object.
     */
    public HttpHeaders setAllHeaders(HttpHeaders headers) {
        if (headers != null) {
            headers.headers.forEach((headerName, header) ->
                setInternal(headerName.getCaseInsensitiveName(), header.getName().getCaseSensitiveName(),
                    header.getValues()));
        }

        return this;
    }

    /**
     * Gets the {@link HttpHeader} for the provided header name. {@code null} is returned if the {@link HttpHeader} isn't found.
     *
     * @param name The name of the {@link HttpHeader} to find.
     *
     * @return The {@link HttpHeader} if found, {@code null} otherwise.
     */
    public HttpHeader get(HttpHeaderName name) {
        return getInternal(name.getCaseInsensitiveName());
    }

    private HttpHeader getInternal(String formattedName) {
        return headers.get(HttpHeaderName.fromString(formattedName));
    }

    /**
     * Removes the {@link HttpHeader} with the provided header name. {@code null} is returned if the {@link HttpHeader} isn't
     * found.
     *
     * @param name The name of the {@link HttpHeader} to remove.
     *
     * @return The {@link HttpHeader} if removed, {@code null} otherwise.
     */
    public HttpHeader remove(HttpHeaderName name) {
        return removeInternal(name.getCaseInsensitiveName());
    }

    private HttpHeader removeInternal(String formattedName) {
        return headers.remove(HttpHeaderName.fromString(formattedName));
    }

    /**
     * Get the value for the provided {@link HttpHeader} name. {@code null} is returned if the {@link HttpHeader} name isn't
     * found.
     *
     * @param name The name of the {@link HttpHeader} whose value is being retrieved.
     *
     * @return The value of the {@link HttpHeader}, or {@code null} if the {@link HttpHeader} isn't found.
     */
    public String getValue(HttpHeaderName name) {
        return getValueInternal(name.getCaseInsensitiveName());
    }

    private String getValueInternal(String formattedName) {
        final HttpHeader header = getInternal(formattedName);

        return header == null ? null : header.getValue();
    }

    /**
     * Get the values for the provided {@link HttpHeader} name. {@code null} is returned if the {@link HttpHeader} name isn't
     * found.
     *
     * <p>This returns {@link #getValue(HttpHeaderName) getValue} split by {@code comma}.</p>
     *
     * @param name The name of the {@link HttpHeader} whose value is being retrieved.
     *
     * @return The values of the {@link HttpHeader}, or {@code null} if the {@link HttpHeader} isn't found.
     */
    public List<String> getValues(HttpHeaderName name) {
        return getValuesInternal(name.getCaseInsensitiveName());
    }

    private List<String> getValuesInternal(String formattedName) {
        final HttpHeader header = getInternal(formattedName);

        return header == null ? null : header.getValues();
    }

    /**
     * Returns a copy of the {@link HttpHeaders} as an unmodifiable {@link Map} representation of the state of the
     * {@link HttpHeaders} at the time of the {@code toMap} call. This map will not change as the underlying {@link HttpHeaders}
     * change, and nor will modifying the key or values contained in the map have any effect on the state of the
     * {@link HttpHeaders}.
     *
     * <p>Note that there may be performance implications of using {@link Map} APIs on the returned {@link Map}. It is
     * highly recommended that users prefer to use alternate APIs present on the {@link HttpHeaders} class, over using APIs
     * present on the returned {@link Map} class. For example, use the {@link #get(HttpHeaderName)} API, rather than
     * {@code headers.toMap().get(name)}.</p>
     *
     * @return The {@link HttpHeaders} in a copied and unmodifiable form.
     */
    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();

        for (final HttpHeader header : headers.values()) {
            result.put(header.getName().getCaseInsensitiveName(), header.getValue());
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
     * Get a {@link Stream} representation of the {@link HttpHeader} values in this instance.
     *
     * @return A {@link Stream} of all {@link HttpHeader} values in this instance.
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
