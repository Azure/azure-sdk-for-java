// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;

import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 * REST response with a streaming content.
 */
public final class StreamResponse extends SimpleResponse<Flux<ByteBuffer>> implements Closeable {
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
    public StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value) {
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
    public Flux<ByteBuffer> getValue() {
        if (response == null) {
            return super.getValue().doFinally(t -> this.consumed = true);
        } else {
            return response.getBody().doFinally(t -> {
                this.consumed = true;
                this.response.close();
            });
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
            final Flux<ByteBuffer> value = getValue();
            value.subscribe().dispose();
        } else {
            response.close();
        }
    }
}
