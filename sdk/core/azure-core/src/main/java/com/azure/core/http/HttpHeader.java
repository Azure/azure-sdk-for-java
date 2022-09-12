// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Header;

import java.util.List;

/**
 * A single header within an HTTP request or response.
 * <p>
 * If multiple header values are added to an HTTP request or response with the same name (case-insensitive), then the
 * values will be appended to the end of the same Header with commas separating them.
 */
public class HttpHeader extends Header {
    private final HttpHeaderName headerName;

    /**
     * Create an HttpHeader instance using the provided name and value.
     *
     * @param name the name
     * @param value the value
     */
    public HttpHeader(String name, String value) {
        this(name, HttpHeaderName.fromString(name), value);
    }

    /**
     * Create an HttpHeader instance using the provided name, headerName, and value.
     *
     * @param name The name
     * @param headerName The HttpHeaderName
     * @param value The value
     */
    public HttpHeader(String name, HttpHeaderName headerName, String value) {
        super(name, value);
        this.headerName = headerName;
    }

    /**
     * Create an HttpHeader instance using the provided name and values, resulting in a single HttpHeader instance with
     * a single name and multiple values set within it.
     *
     * @param name the name
     * @param values the values
     */
    public HttpHeader(String name, List<String> values) {
        this(name, HttpHeaderName.fromString(name), values);
    }

    /**
     * Create an HttpHeader instance using the provided name, headerName, and value.
     *
     * @param name The name
     * @param headerName The HttpHeaderName
     * @param values The values
     */
    public HttpHeader(String name, HttpHeaderName headerName, List<String> values) {
        super(name, values);
        this.headerName = headerName;
    }

    /**
     * Gets the HTTP header name.
     *
     * @return The HTTP name of this {@link HttpHeader}.
     */
    public HttpHeaderName getHeaderName() {
        return headerName;
    }
}
