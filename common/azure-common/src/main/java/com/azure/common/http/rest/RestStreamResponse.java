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
public final class RestStreamResponse extends SimpleRestResponse<Flux<ByteBuf>> implements Closeable {
    /**
     * Creates RestStreamResponse.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param headers the headers of the HTTP response
     * @param body the streaming body
     */
    public RestStreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuf> body) {
        super(request, statusCode, headers, body);
    }

    /**
     * @return the stream content
     */
    @Override
    public Flux<ByteBuf> body() {
        return super.body();
    }

    /**
     * Disposes the connection associated with this RestStreamResponse.
     */
    @Override
    public void close() {
        body().subscribe().dispose();
    }
}
