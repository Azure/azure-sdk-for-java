/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of headers that will be applied to a HTTP request.
 */
public class HttpHeaders implements Iterable<HttpHeader> {
    private final Map<String, HttpHeader> headers = new HashMap<>();

    /**
     * Add the provided headerName and headerValue to the list of headers for this request.
     * @param headerName The name of the header.
     * @param headerValue The value of the header.
     * @return This HttpRequest so that multiple operations can be chained together.
     */
    public HttpHeaders add(String headerName, String headerValue) {
        final String headerKey = headerName.toLowerCase();
        if (!headers.containsKey(headerKey)) {
            headers.put(headerKey, new HttpHeader(headerName, headerValue));
        }
        else {
            headers.get(headerKey).addValue(headerValue);
        }
        return this;
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        return headers.values().iterator();
    }
}
