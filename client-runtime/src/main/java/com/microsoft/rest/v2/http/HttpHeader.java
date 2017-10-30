/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

/**
 * A single header within a HTTP request. If multiple header values are added to a HTTP request with
 * the same name (case-insensitive), then the values will be appended to the end of the same Header
 * with commas separating them.
 */
public class HttpHeader {
    private final String name;
    private String value;

    /**
     * Create a new HttpHeader using the provided name and value.
     * @param name The name of the HttpHeader.
     * @param value The value of the HttpHeader.
     */
    public HttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of this Header.
     * @return The name of this Header.
     */
    public String name() {
        return name;
    }

    /**
     * Get the value of this Header.
     * @return The value of this Header.
     */
    public String value() {
        return value;
    }

    /**
     * Get the values of this Header that are separated by a comma.
     * @return The values of this Header that are separated by a comma.
     */
    public String[] values() {
        return value == null ? null : value.split(",");
    }

    /**
     * Add another value to the end of this Header.
     * @param value The value to add to the end of this Header.
     */
    public void addValue(String value) {
        this.value += "," + value;
    }

    /**
     * Get the String representation of this HttpHeader.
     * @return The String representation of this HttpHeader.
     */
    @Override
    public String toString() {
        return name + ":" + value;
    }
}
