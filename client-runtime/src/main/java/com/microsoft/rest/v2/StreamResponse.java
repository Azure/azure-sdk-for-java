/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.v2.http.HttpRequest;
import io.reactivex.Flowable;
import io.reactivex.internal.functions.Functions;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * A response to a REST call with a streaming body.
 */
public final class StreamResponse extends RestResponse<Void, Flowable<ByteBuffer>> implements Closeable {
    /**
     * Creates a StreamResponse.
     *
     * @param request the request which resulted in this StreamResponse
     * @param statusCode the status code of the HTTP response
     * @param rawHeaders the raw headers of the HTTP response
     * @param body the streaming body
     */
    public StreamResponse(HttpRequest request, int statusCode, Map<String, String> rawHeaders, Flowable<ByteBuffer> body) {
        super(request, statusCode, null, rawHeaders, body);
    }

    // Used for uniform reflective creation in RestProxy.
    @SuppressWarnings("unused")
    StreamResponse(HttpRequest request, int statusCode, Void headers, Map<String, String> rawHeaders, Flowable<ByteBuffer> body) {
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
     * @return the body content stream
     */
    @Override
    public Flowable<ByteBuffer> body() {
        return super.body();
    }

    /**
     * Disposes of the connection associated with this StreamResponse.
     */
    @Override
    public void close() {
        body().subscribe(Functions.emptyConsumer(), Functions.<Throwable>emptyConsumer()).dispose();
    }
}
