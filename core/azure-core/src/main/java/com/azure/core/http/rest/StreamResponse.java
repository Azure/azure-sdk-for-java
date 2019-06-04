// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;

import java.io.Closeable;

/**
 * REST response with a streaming content.
 */
public final class StreamResponse extends SimpleResponse<Flux<ByteBuf>> implements Closeable {
    /**
     * Creates StreamResponse.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param headers the headers of the HTTP response
     * @param value the streaming value
     */
    public StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuf> value) {
        super(request, statusCode, headers, value);
    }

    /**
     * @return the stream content
     */
    @Override
    public Flux<ByteBuf> value() {
        return super.value();
    }

    /**
     * Disposes the connection associated with this StreamResponse.
     */
    @Override
    public void close() {
        value().subscribe().dispose();
    }
}
