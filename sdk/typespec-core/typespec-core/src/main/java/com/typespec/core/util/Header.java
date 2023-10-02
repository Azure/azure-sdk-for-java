// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Represents a single header to be set on a request.
 * <p>
 * If multiple header values are added to a request with the same name (case-insensitive), then the values will be
 * appended at the end of the same {@link Header} with commas separating them.
 */
public class Header {
    private static final String[] EMPTY_HEADER_ARRAY = new String[0];

    private final String name;

    // This is the internal representation of a single value.
    private String value;

    // This is the internal representation of multiple values.
    private List<String> values;

    // but we also cache it to faster serve our public API
    private volatile String cachedStringValue;
    private static final AtomicReferenceFieldUpdater<Header, String> CACHED_STRING_VALUE_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(Header.class, String.class, "cachedStringValue");

    /**
     * Create a Header instance using the provided name and value.
     *
     * @param name the name of the header.
     * @param value the value of the header.
     * @throws NullPointerException if {@code name} is null.
     */
    public Header(String name, String value) {
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
    public Header(String name, String... values) {
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
    public Header(String name, List<String> values) {
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
     * @return the name of this {@link Header}
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the combined, comma-separated value of this {@link Header}, taking into account all values provided.
     *
     * @return the value of this Header
     */
    public String getValue() {
        if (value != null) {
            return value;
        } else if (CoreUtils.isNullOrEmpty(values)) {
            return "";
        }

        checkCachedStringValue();
        return CACHED_STRING_VALUE_UPDATER.get(this);
    }

    /**
     * Gets the comma separated value as an array. Changes made to this array will not be reflected in the headers.
     *
     * @return the values of this {@link Header} that are separated by a comma
     */
    public String[] getValues() {
        if (value != null) {
            return new String[] {value};
        } else if (!CoreUtils.isNullOrEmpty(values)) {
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
    public List<String> getValuesList() {
        if (value != null) {
            return Collections.singletonList(value);
        } else if (!CoreUtils.isNullOrEmpty(values)) {
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
     * Gets the String representation of the header.
     *
     * @return the String representation of this Header.
     */
    @Override
    public String toString() {
        if (value != null) {
            return name + ":" + value;
        } else if (CoreUtils.isNullOrEmpty(values)) {
            return "";
        }

        checkCachedStringValue();
        return name + ":" + CACHED_STRING_VALUE_UPDATER.get(this);
    }

    private void checkCachedStringValue() {
        CACHED_STRING_VALUE_UPDATER.compareAndSet(this, null, CoreUtils.stringJoin(",", values));
    }
}
