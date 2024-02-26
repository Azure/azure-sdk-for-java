// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Header;

import java.util.List;

/**
 * Represents a single header within an HTTP request or response.
 *
 * <p>This class encapsulates the name and value(s) of an HTTP header. If multiple values are associated with the same
 * header name, they are stored in a single HttpHeader instance with values separated by commas.</p>
 *
 * <p>It provides constructors to create an HttpHeader instance with a single value {@link #HttpHeader(String, String)}
 * or multiple values {@link #HttpHeader(String, List)}.</p>
 *
 * <p>This class is useful when you want to work with individual headers of an HTTP request or response.</p>
 *
 * <p>Note: Header names are case-insensitive.</p>
 */
public class HttpHeader extends Header {
    /**
     * Create an HttpHeader instance using the provided name and value.
     *
     * @param name the name
     * @param value the value
     */
    public HttpHeader(String name, String value) {
        super(name, value);
    }

    /**
     * Create an HttpHeader instance using the provided name and values, resulting in a single HttpHeader instance with
     * a single name and multiple values set within it.
     *
     * @param name the name
     * @param values the values
     */
    public HttpHeader(String name, List<String> values) {
        super(name, values);
    }
}
