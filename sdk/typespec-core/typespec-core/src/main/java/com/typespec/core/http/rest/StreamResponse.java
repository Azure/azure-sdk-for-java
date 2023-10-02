// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.typespec.core.http.rest;

import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
public final class StreamResponse extends SimpleResponse<Flux<ByteBuffer>> implements Closeable {
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
     * Transfers content bytes to the {@link AsynchronousByteChannel}.
     * @param channel The destination {@link AsynchronousByteChannel}.
     * @return A {@link Mono} that completes when transfer is completed.
     */
    public Mono<Void> writeValueToAsync(AsynchronousByteChannel channel) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        if (response == null) {
            return FluxUtil.writeToAsynchronousByteChannel(getValue(), channel);
        } else {
            return response.writeBodyToAsync(channel);
        }
    }

    /**
     * Transfers content bytes to the {@link WritableByteChannel}.
     * @param channel The destination {@link WritableByteChannel}.
     * @throws UncheckedIOException When I/O operation fails.
     */
    public void writeValueTo(WritableByteChannel channel) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        if (response == null) {
            FluxUtil.writeToWritableByteChannel(getValue(), channel).block();
        } else {
            try {
                response.writeBodyTo(channel);
            } catch (IOException ex) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
            }
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
