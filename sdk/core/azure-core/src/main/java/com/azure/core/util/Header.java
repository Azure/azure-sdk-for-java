// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Objects;

/**
 * Represents a single header to be set on a request.
 *<p>
 * If multiple header values are added to a request with the same name (case-insensitive), then the values will be
 * appended at the end of the same {@link Header} with commas separating them.
 */
public class Header {
    private final String name;
    private String value;

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
     * Gets the header name.
     *
     * @return the name of this {@link Header}
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of this {@link Header}.
     *
     * @return the value of this Header
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the comma separated value as an array.
     *
     * @return the values of this {@link Header} that are separated by a comma
     */
    public String[] getValues() {
        return value == null ? null : value.split(",");
    }

    /**
     * Add a new value to the end of the Header.
     *
     * @param value the value to add
     */
    public void addValue(String value) {
        if (this.value != null) {
            this.value += "," + value;
        } else {
            this.value = value;
        }
    }

    /**
     * Gets the String representation of the header.
     *
     * @return the String representation of this Header.
     */
    @Override
    public String toString() {
        return name + ":" + value;
    }
}
