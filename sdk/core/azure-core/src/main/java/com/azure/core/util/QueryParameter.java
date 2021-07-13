// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

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
public class QueryParameter {
    private final String name;

    // this is the actual internal representation of all values
    private final List<String> values;

    // but we also cache it to faster serve our public API
    private String cachedStringValue;

    /**
     * Create a QueryParameter instance using the provided name and value.
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @throws NullPointerException if {@code name} is null.
     */
    public QueryParameter(String name, String value) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        this.name = name;
        this.values = new LinkedList<>();
        this.values.add(value);
    }

    /**
     * Create a QueryParameter instance using the provided name and values.
     *
     * @param name the name of the parameter.
     * @param values the values of the parameter.
     * @throws NullPointerException if {@code name} is null.
     */
    public QueryParameter(String name, String... values) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        this.name = name;
        this.values = new LinkedList<>();
        for (String value : values) {
            this.values.add(value);
        }
    }

    /**
     * Create a QueryParameter instance using the provided name and values.
     *
     * @param name the name of the parameter.
     * @param values the values of the parameter.
     * @throws NullPointerException if {@code name} is null.
     */
    public QueryParameter(String name, List<String> values) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        this.name = name;
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
        return values.toArray(new String[] { });
    }

    /**
     * Returns all values associated with this parameter, represented as an unmodifiable list of strings.
     *
     * @return An unmodifiable list containing all values associated with this parameter.
     */
    public List<String> getValuesList() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Add a new value to the end of the QueryParameter.
     *
     * @param value the value to add
     */
    public void addValue(String value) {
        this.values.add(value);
        this.cachedStringValue = null;
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
            cachedStringValue = String.join(",", values);
        }
    }
}
