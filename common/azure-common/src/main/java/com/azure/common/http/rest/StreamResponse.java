/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.common.http.rest;

import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpRequest;
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
     * @param result the streaming result
     */
    public StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuf> result) {
        super(request, statusCode, headers, result);
    }

    /**
     * @return the stream content
     */
    @Override
    public Flux<ByteBuf> result() {
        return super.result();
    }

    /**
     * Disposes the connection associated with this StreamResponse.
     */
    @Override
    public void close() {
        result().subscribe().dispose();
    }
}
