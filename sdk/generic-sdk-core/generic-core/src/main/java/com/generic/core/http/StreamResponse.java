// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.implementation.http.SimpleStreamResponse;
import com.generic.core.models.Headers;

import java.io.InputStream;

/**
 * REST response with streaming content.
 */
public interface StreamResponse extends Response<InputStream> {
    /**
     * Disposes the connection associated with this {@link StreamResponse}.
     */
    void close();

    /**
     * A static method that creates a default {@link StreamResponse} implementation backed by the given
     * {@link InputStream}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the response.
     * @param headers The headers of the response.
     * @param value The {@link InputStream} which contains the response content.
     *
     * @return A default {@link StreamResponse} implementation backed by the given {@link InputStream}.
     */
    static Response<InputStream> create(HttpRequest request, int statusCode, Headers headers, InputStream value) {
        return new SimpleStreamResponse(request, statusCode, headers, value);
    }
}
