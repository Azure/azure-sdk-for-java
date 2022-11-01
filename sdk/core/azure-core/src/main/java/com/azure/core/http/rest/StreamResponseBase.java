// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
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
 * REST response with a streaming content and typed headers object.
 *
 * @param <HEADERS> The typed headers object.
 */
public final class StreamResponseBase<HEADERS> extends ResponseBase<HEADERS, Flux<ByteBuffer>> implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(StreamResponseBase.class);

    private volatile boolean consumed;
    private final HttpResponse response;

    /**
     * Creates a new instance of {@link StreamResponseBase}.
     *
     * @param response The HTTP response.
     * @param headers The typed headers object.
     */
    public StreamResponseBase(HttpResponse response, HEADERS headers) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null, headers);
        this.response = response;
    }

    /**
     * The content of the HTTP response as a stream of {@link ByteBuffer byte buffers}.
     *
     * @return The content of the HTTP response as a stream of {@link ByteBuffer byte buffers}.
     */
    @Override
    public Flux<ByteBuffer> getValue() {
        return response.getBody().doFinally(t -> {
            this.consumed = true;
            this.response.close();
        });
    }

    /**
     * Transfers content bytes to the {@link AsynchronousByteChannel}.
     *
     * @param channel The destination {@link AsynchronousByteChannel}.
     * @return A {@link Mono} that completes when transfer is completed.
     * @throws NullPointerException If {@code channel} is null.
     */
    public Mono<Void> writeValueToAsync(AsynchronousByteChannel channel) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        return response.writeBodyToAsync(channel);
    }

    /**
     * Transfers content bytes to the {@link WritableByteChannel}.
     *
     * @param channel The destination {@link WritableByteChannel}.
     * @throws NullPointerException If {@code channel} is null.
     * @throws UncheckedIOException When I/O operation fails.
     */
    public void writeValueTo(WritableByteChannel channel) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        try {
            response.writeBodyTo(channel);
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
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
        response.close();
    }
}
