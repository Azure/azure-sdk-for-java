// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single parameter to be added to a query string.
 *<p>
 * If multiple values are added to a query string with the same name (case-insensitive), then the values will be
 * appended at the end of the same {@link QueryParameter} with commas separating them.
 */
class QueryParameter {
    private final String name;

    // this is the internal representation of a single value
    // for this parameter. this is the common case (vs. having name=a&name=b etc.)
    private final String value;

    // this is the actual internal representation of all values
    // in case we have name=a&name=b&name=c
    private List<String> values;

    // but we also cache it to faster serve our public API
    private volatile String cachedStringValue;

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
        this.value = null;
        this.values = new LinkedList<>(values);
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
        checkCachedStringValue();
        return cachedStringValue;
    }

    /**
     * Gets the comma separated value as an array. Changes made to this array will not be reflected in the parameters.
     *
     * @return the values of this {@link QueryParameter} that are separated by a comma
     */
    public String[] getValues() {
        if (values == null) {
            // most common case
            return new String[] {value};
        } else {
            return values.toArray(new String[] { });
        }
    }

    /**
     * Returns all values associated with this parameter, represented as an unmodifiable list of strings.
     *
     * @return An unmodifiable list containing all values associated with this parameter.
     */
    public List<String> getValuesList() {
        if (values == null) {
            // most common case is that we don't have a list of values, but a single one
            // a convenience return value is implemented here to avoid NPEs.
            // List.of() would be a better option but it is Java 9+ only.
            return Collections.unmodifiableList(Arrays.asList(value));
        } else {
            return Collections.unmodifiableList(values);
        }
    }

    /**
     * Add a new value to the end of the QueryParameter.
     *
     * @param newValue the value to add
     */
    public void addValue(String newValue) {
        if (values == null) {
            values = new LinkedList<>();
            // add current standalone value to the list
            // as the list is empty
            values.add(this.value);
        }

        // add additional value to the parameter value list
        values.add(newValue);
        cachedStringValue = null;
    }

    /**
     * Gets the String representation of the parameter.
     *
     * @return the String representation of this QueryParameter.
     */
    @Override
    public String toString() {
        checkCachedStringValue();
        return name + "=" + cachedStringValue;
    }

    private void checkCachedStringValue() {
        if (cachedStringValue == null) {
            if (values == null) {
                cachedStringValue = value;
            } else {
                cachedStringValue = String.join(",", values);
            }
        }
    }
}
