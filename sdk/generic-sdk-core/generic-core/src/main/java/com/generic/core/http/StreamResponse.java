// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.generic.core.http;


import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Headers;
import com.generic.core.util.logging.ClientLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

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
     * @param headers The headers of the HTTP response.
     * @param value The content of the HTTP response.
     * @deprecated Use {@link #StreamResponse(HttpResponse)}
     */
    @Deprecated
    public StreamResponse(HttpRequest request, int statusCode, Headers headers, ByteBuffer value) {
        super(request, statusCode, headers, value);
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
