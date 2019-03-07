/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3;

import com.microsoft.rest.v3.http.HttpRequest;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;

import java.io.Closeable;
import java.util.Map;

/**
 * REST response with a streaming content.
 */
public final class RestStreamResponse extends RestResponseBase<Void, Flux<ByteBuf>> implements Closeable {
    /**
     * Creates RestStreamResponse.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param rawHeaders the raw headers of the HTTP response
     * @param body the streaming body
     */
    public RestStreamResponse(HttpRequest request, int statusCode, Map<String, String> rawHeaders, Flux<ByteBuf> body) {
        super(request, statusCode, null, rawHeaders, body);
    }

    // Used for uniform reflective creation in RestProxy.
    @SuppressWarnings("unused")
    RestStreamResponse(HttpRequest request, int statusCode, Void headers, Map<String, String> rawHeaders, Flux<ByteBuf> body) {
        super(request, statusCode, headers, rawHeaders, body);
    }

    /**
     * Always returns null due to no headers type being defined in the service specification.
     * Consider using {@link #rawHeaders()}.
     *
     * @return null
     */
    @Override
    public Void headers() {
        return super.headers();
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
