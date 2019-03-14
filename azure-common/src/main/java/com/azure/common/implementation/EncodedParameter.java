/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation;

/**
 * Type representing result of encoding a query parameter or header name/value pair for a
 * HTTP request. It contains the query parameter or header name, plus the query parameter's value or
 * header's value.
 */
class EncodedParameter {
    private final String name;
    private final String encodedValue;

    /**
     * Create a EncodedParameter using the provided parameter name and encoded value.
     *
     * @param name the name of the new parameter
     * @param encodedValue the encoded value of the new parameter
     */
    EncodedParameter(String name, String encodedValue) {
        this.name = name;
        this.encodedValue = encodedValue;
    }

    /**
     * Get this parameter's name.
     *
     * @return the name of this parameter
     */
    public String name() {
        return name;
    }

    /**
     * Get the encoded value for this parameter.
     *
     * @return the encoded value for this parameter
     */
    public String encodedValue() {
        return encodedValue;
    }
}
