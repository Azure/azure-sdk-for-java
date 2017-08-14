/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

/**
 * An EncodedParameter is the result of encoding a query parameter or header name/value pair for a
 * HTTP request. It contains the query parameter or header name, plus the query parameter's value or
 * header's value.
 */
public class EncodedParameter {
    private final String name;
    private final String encodedValue;

    /**
     * Create a new EncodedParameter using the provided parameter name and encoded value.
     * @param name The name of the new parameter.
     * @param encodedValue The encoded value of the new parameter.
     */
    public EncodedParameter(String name, String encodedValue) {
        this.name = name;
        this.encodedValue = encodedValue;
    }

    /**
     * Get this parameter's name.
     * @return The name of this parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the encoded value for this parameter.
     * @return The encoded value for this parameter.
     */
    public String getEncodedValue() {
        return encodedValue;
    }

    /**
     * Get whether or not this value equals the provided rhs value.
     * @param rhs The value to compare against this value.
     * @return Whether or not this value equals the provided rhs value.
     */
    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof EncodedParameter ? equals((EncodedParameter) rhs) : false;
    }

    /**
     * Get whether or not this value equals the provided rhs value.
     * @param rhs The value to compare against this value.
     * @return Whether or not this value equals the provided rhs value.
     */
    public boolean equals(EncodedParameter rhs) {
        return rhs != null
                && name.equals(rhs.name)
                && encodedValue.equals(rhs.encodedValue);
    }

    /**
     * Get the unique hash code for this value.
     * @return The unique hash code for this value.
     */
    @Override
    public int hashCode() {
        return name.hashCode() ^ encodedValue.hashCode();
    }
}
