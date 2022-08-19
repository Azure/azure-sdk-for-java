// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Collections;
import java.util.LinkedList;
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
    private final String name;

    // this is the actual internal representation of all values
    private final List<String> values;

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
        this.values = new LinkedList<>();
        this.values.add(value);
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
        this.values = new LinkedList<>();
        Collections.addAll(this.values, values);
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
        this.values = new LinkedList<>(values);
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
        checkCachedStringValue();
        return CACHED_STRING_VALUE_UPDATER.get(this);
    }

    /**
     * Gets the comma separated value as an array. Changes made to this array will not be reflected in the headers.
     *
     * @return the values of this {@link Header} that are separated by a comma
     */
    public String[] getValues() {
        return values.toArray(new String[0]);
    }

    /**
     * Returns all values associated with this header, represented as an unmodifiable list of strings.
     *
     * @return An unmodifiable list containing all values associated with this header.
     */
    public List<String> getValuesList() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Add a new value to the end of the Header.
     *
     * @param value the value to add
     */
    public void addValue(String value) {
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
        checkCachedStringValue();
        return name + ":" + CACHED_STRING_VALUE_UPDATER.get(this);
    }

    private void checkCachedStringValue() {
        CACHED_STRING_VALUE_UPDATER.compareAndSet(this, null, getOrConcatenate(values));
    }

    /*
     * Optimization over just using String.join.
     *
     * In most cases the header will only have a few values which can be combined statically instead of needing have
     * all the overhead from String.join.
     *
     */
    private static String getOrConcatenate(List<String> values) {
        int size = values.size();
        switch (size) {
            case 0: return "";
            case 1: return values.get(0);
            case 2: return values.get(0) + "," + values.get(1);
            case 3: return values.get(0) + "," + values.get(1) + "," + values.get(2);
            case 4: return values.get(0) + "," + values.get(1) + "," + values.get(2) + "," + values.get(3);
            default: return String.join(",", values);
        }
    }
}
