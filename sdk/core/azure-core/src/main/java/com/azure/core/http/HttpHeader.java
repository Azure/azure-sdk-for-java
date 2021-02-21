// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Header;

import java.util.List;

/**
 * A single header within a HTTP request or response.
 *
 * If multiple header values are added to a HTTP request or response with
 * the same name (case-insensitive), then the values will be appended
 * to the end of the same Header with commas separating them.
 */
public class HttpHeader extends Header {

    /**
     * Create a HttpHeader instance using the provided name and value.
     *
     * @param name the name
     * @param value the value
     */
    public HttpHeader(String name, String value) {
        super(name, value);
    }

    /**
     * Create a HttpHeader instance using the provided name and values, resulting in a single HttpHeader instance with
     * a single name and multiple values set within it.
     *
     * @param name the name
     * @param values the values
     */
    public HttpHeader(String name, List<String> values) {
        super(name, values);
    }
}
