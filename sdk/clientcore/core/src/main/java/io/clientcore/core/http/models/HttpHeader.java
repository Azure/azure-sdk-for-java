// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;
import static io.clientcore.core.implementation.util.ImplUtils.stringJoin;

/**
 * Represents a single header to be set on a request.
 * <p>
 * If multiple header values are added to a request with the same name (case-insensitive), then the values will be
 * appended at the end of the same {@link HttpHeader} with commas separating them.
 */
public class HttpHeader {
    private static final String[] EMPTY_HEADER_ARRAY = new String[0];

    private final HttpHeaderName name;

    // This is the internal representation of a single value.
    private String value;

    // This is the internal representation of multiple values.
    private List<String> values;

    // but we also cache it to faster serve our public API
    private volatile String cachedStringValue;
    private static final AtomicReferenceFieldUpdater<HttpHeader, String> CACHED_STRING_VALUE_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(HttpHeader.class, String.class, "cachedStringValue");

    /**
     * Create a Header instance using the provided name and value.
     *
     * @param name the {@link HttpHeaderName name} of the header.
     * @param value the value of the header.
     * @throws NullPointerException if {@code name} is null.
     */
    public HttpHeader(HttpHeaderName name, String value) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        this.name = name;
        this.value = value;
    }

    /**
     * Create a Header instance using the provided name and values.
     *
     * @param name the name of the header.
     * @param values the values of the header.
     * @throws NullPointerException if {@code name} is null.
     */
    public HttpHeader(HttpHeaderName name, String... values) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        this.name = name;
        int length = values.length;
        if (length == 1) {
            this.value = values[0];
        } else if (length != 0) {
            this.values = new ArrayList<>(Math.max(length + 2, 4));
            Collections.addAll(this.values, values);
        }
    }

    /**
     * Create a Header instance using the provided name and values.
     *
     * @param name the name of the header.
     * @param values the values of the header.
     * @throws NullPointerException if {@code name} is null.
     */
    public HttpHeader(HttpHeaderName name, List<String> values) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        this.name = name;
        int size = values.size();
        if (size == 1) {
            this.value = values.get(0);
        } else if (size != 0) {
            this.values = new ArrayList<>(Math.max(size + 2, 4));
            this.values.addAll(values);
        }
    }

    /**
     * Gets the header name.
     *
     * @return the {@link HttpHeaderName name} of this {@link HttpHeader}
     */
    public HttpHeaderName getName() {
        return name;
    }

    /**
     * Gets the combined, comma-separated value of this {@link HttpHeader}, taking into account all values provided.
     *
     * @return the value of this Header
     */
    public String getValue() {
        if (value != null) {
            return value;
        } else if (isNullOrEmpty(values)) {
            return "";
        }

        checkCachedStringValue();
        return CACHED_STRING_VALUE_UPDATER.get(this);
    }

    /**
     * Gets the comma separated value as an array. Changes made to this array will not be reflected in the headers.
     *
     * @return the values of this {@link HttpHeader} that are separated by a comma
     */
    String[] getValuesArray() {
        if (value != null) {
            return new String[] {value};
        } else if (!isNullOrEmpty(values)) {
            return values.toArray(new String[0]);
        } else {
            return EMPTY_HEADER_ARRAY;
        }
    }

    /**
     * Returns all values associated with this header, represented as an unmodifiable list of strings.
     *
     * @return An unmodifiable list containing all values associated with this header.
     */
    public List<String> getValues() {
        if (value != null) {
            return Collections.singletonList(value);
        } else if (!isNullOrEmpty(values)) {
            return Collections.unmodifiableList(values);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Add a new value to the end of the Header.
     *
     * @param value the value to add
     */
    public void addValue(String value) {
        if (this.value == null && values == null) {
            this.value = value;
            return;
        } else if (values == null) {
            values = new ArrayList<>(4); // 4 was selected to add a buffer of 2 as seen in the constructor.
            values.add(this.value);
            this.value = null;
        }

        this.values.add(value);
        CACHED_STRING_VALUE_UPDATER.set(this, null);
    }

    /**
     * Add a new value to the end of the Header.
     *
     * @param values the value to add
     */
    public void addValues(List<String> values) {
        if (isNullOrEmpty(values)) {
            return;
        }

        if (this.value == null && this.values == null) {
            this.values = new ArrayList<>(values);
            return;
        } else if (this.values == null) {
            this.values = new ArrayList<>(values.size() + 1);
            values.add(this.value);
            this.value = null;
        }

        this.values.addAll(values);
        CACHED_STRING_VALUE_UPDATER.set(this, null);
    }

    /**
     * Gets the String representation of the header.
     *
     * @return the String representation of this Header.
     */
    @Override
    public String toString() {
        if (value != null) {
            return name + ":" + value;
        } else if (isNullOrEmpty(values)) {
            return "";
        }

        checkCachedStringValue();
        return name + ":" + CACHED_STRING_VALUE_UPDATER.get(this);
    }

    private void checkCachedStringValue() {
        CACHED_STRING_VALUE_UPDATER.compareAndSet(this, null, stringJoin(",", values));
    }
}
