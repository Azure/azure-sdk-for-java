/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.http;

/**
 * A single header within a HTTP request or response.
 *
 * If multiple header values are added to a HTTP request or response with
 * the same name (case-insensitive), then the values will be appended
 * to the end of the same Header with commas separating them.
 */
public class HttpHeader {
    private final String name;
    private String value;

    /**
     * Create a HttpHeader instance using the provided name and value.
     *
     * @param name the name
     * @param value the value
     */
    public HttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the header name.
     *
     * @return the name of this Header
     */
    public String name() {
        return name;
    }

    /**
     * Get the header value.
     *
     * @return the value of this Header
     */
    public String value() {
        return value;
    }

    /**
     * Get the comma separated value as an array.
     *
     * @return the values of this Header that are separated by a comma
     */
    public String[] values() {
        return value == null ? null : value.split(",");
    }

    /**
     * Add a new value to the end of the Header.
     *
     * @param value the value to add
     */
    public void addValue(String value) {
        this.value += "," + value;
    }

    /**
     * Get the String representation of the header.
     *
     * @return the String representation of this HttpHeader
     */
    @Override
    public String toString() {
        return name + ":" + value;
    }
}
