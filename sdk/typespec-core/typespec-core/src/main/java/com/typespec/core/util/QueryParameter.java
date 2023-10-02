// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Represents a single parameter to be added to a query string.
 *<p>
 * If multiple values are added to a query string with the same name (case-insensitive), then the values will be
 * appended at the end of the same {@link QueryParameter} with commas separating them.
 */
class QueryParameter {
    private static final String[] EMPTY_QUERY_PARAMETER_ARRAY = new String[0];

    private final String name;

    // this is the internal representation of a single value
    // for this parameter. this is the common case (vs. having name=a&name=b etc.)
    private String value;

    // this is the actual internal representation of all values
    // in case we have name=a&name=b&name=c
    private List<String> values;

    // but we also cache it to faster serve our public API
    private volatile String cachedStringValue;
    private static final AtomicReferenceFieldUpdater<QueryParameter, String> CACHED_STRING_VALUE_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(QueryParameter.class, String.class, "cachedStringValue");

    /**
     * Create a QueryParameter instance using the provided name and value.
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @throws NullPointerException if {@code name} is null.
     */
    QueryParameter(String name, String value) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        this.name = name;
        this.value = value;
    }

    /**
     * Create a QueryParameter instance using the provided name and values.
     *
     * @param name the name of the parameter.
     * @param values the values of the parameter.
     * @throws NullPointerException if {@code name} or {@code values} are null.
     */
    QueryParameter(String name, List<String> values) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(values, "'values' cannot be null");
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
     * Gets the parameter name.
     *
     * @return the name of this {@link QueryParameter}
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the combined, comma-separated value of this {@link QueryParameter}, taking into account all values provided.
     *
     * @return the value of this QueryParameter
     */
    public String getValue() {
        if (value != null) {
            return value;
        } else if (CoreUtils.isNullOrEmpty(values)) {
            return "";
        }

        checkCachedStringValue();
        return cachedStringValue;
    }

    /**
     * Gets the comma separated value as an array. Changes made to this array will not be reflected in the parameters.
     *
     * @return the values of this {@link QueryParameter} that are separated by a comma
     */
    public String[] getValues() {
        if (value != null) {
            return new String[] {value};
        } else if (!CoreUtils.isNullOrEmpty(values)) {
            return values.toArray(new String[0]);
        } else {
            return EMPTY_QUERY_PARAMETER_ARRAY;
        }
    }

    /**
     * Returns all values associated with this parameter, represented as an unmodifiable list of strings.
     *
     * @return An unmodifiable list containing all values associated with this parameter.
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
     * Add a new value to the end of the QueryParameter.
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
     * Gets the String representation of the parameter.
     *
     * @return the String representation of this QueryParameter.
     */
    @Override
    public String toString() {
        if (value != null) {
            return name + "=" + value;
        } else if (CoreUtils.isNullOrEmpty(values)) {
            return "";
        }

        checkCachedStringValue();
        return name + "=" + CACHED_STRING_VALUE_UPDATER.get(this);
    }

    private void checkCachedStringValue() {
        CACHED_STRING_VALUE_UPDATER.compareAndSet(this, null, CoreUtils.stringJoin(",", values));
    }
}
