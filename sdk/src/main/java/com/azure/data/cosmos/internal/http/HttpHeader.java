/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.internal.http;

import org.apache.commons.lang3.StringUtils;

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
        return value == null ? null : StringUtils.split(value, ",");
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
