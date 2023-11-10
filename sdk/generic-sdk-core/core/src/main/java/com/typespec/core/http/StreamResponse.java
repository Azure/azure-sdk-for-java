// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.typespec.core.http;


import com.typespec.core.http.models.HttpRequest;
import com.typespec.core.http.models.HttpResponse;
import com.typespec.core.http.models.HttpHeaders;
import com.typespec.core.util.ClientLogger;

import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 * REST response with a streaming content.
 */
public final class StreamResponse extends SimpleResponse<ByteBuffer> implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(StreamResponse.class);

    private volatile boolean consumed;
    private final HttpResponse response;

    /**
     * Creates a {@link StreamResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param httpHeaders The headers of the HTTP response.
     * @param value The content of the HTTP response.
     * @deprecated Use {@link #StreamResponse(HttpResponse)}
     */
    @Deprecated
    public StreamResponse(HttpRequest request, int statusCode, HttpHeaders httpHeaders, ByteBuffer value) {
        super(request, statusCode, httpHeaders, value);
        response = null;
    }

    /**
     * Creates a {@link StreamResponse}.
     *
     * @param response The HTTP response.
     */
    public StreamResponse(HttpResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        this.response = response;
    }

    /**
     * The content of the HTTP response as a stream of {@link ByteBuffer byte buffers}.
     *
     * @return The content of the HTTP response as a stream of {@link ByteBuffer byte buffers}.
     */
    @Override
    public ByteBuffer getValue() {
        if (response == null) {
            return super.getValue();
        } else {
            return response.getBody().toByteBuffer();
        }
    }

    /**
     * Disposes the connection associated with this {@link StreamResponse}.
     */
    @Override
    public void close() {
        if (this.consumed) {
            return;
        }
        this.consumed = true;
        if (response == null) {
            final ByteBuffer value = getValue();
            value.clear();
        } else {
            response.close();
        }
    }
}
