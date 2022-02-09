// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final ClientLogger logger = new ClientLogger(BufferedHttpResponse.class);

    private final HttpResponse innerHttpResponse;
    private final Mono<List<ByteBuffer>> cachedBody;
    private final AtomicLong cachedBodySize = new AtomicLong();

    /**
     * Creates a buffered HTTP response.
     *
     * @param innerHttpResponse The HTTP response to buffer
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse) {
        super(innerHttpResponse.getRequest());
        this.innerHttpResponse = innerHttpResponse;
        this.cachedBody = innerHttpResponse.getBody()
            .map(buffer -> {
                cachedBodySize.addAndGet(buffer.remaining());
                return ByteBuffer.wrap(FluxUtil.byteBufferToArray(buffer));
            })
            .collectList()
            .cache();
    }

    @Override
    public int getStatusCode() {
        return innerHttpResponse.getStatusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return innerHttpResponse.getHeaderValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return innerHttpResponse.getHeaders();
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return cachedBody.flatMapMany(Flux::fromIterable).map(ByteBuffer::duplicate);
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        // Check that the body would fit into a byte array before spending time to create the merged byte array.
        return (cachedBodySize.get() > Integer.MAX_VALUE)
            ? monoError(logger, new IllegalStateException(
                "Response with body size " + cachedBodySize.get() + " doesn't fit into a byte array."))
            : FluxUtil.collectBytesInByteBufferStream(getBody(), (int) cachedBodySize.get());
    }

    @Override
    public Mono<String> getBodyAsString() {
        // Check that the body would fit into a String before spending the time to create the String.
        return (cachedBodySize.get() > Integer.MAX_VALUE)
            ? monoError(logger, new IllegalStateException(
                "Response with body size " + cachedBodySize.get() + " doesn't fit into a String."))
            : getBodyAsByteArray().map(bytes ->
                CoreUtils.bomAwareToString(bytes, innerHttpResponse.getHeaderValue("Content-Type")));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        // Check that the body would fit into a String before spending the time to create the String.
        return (cachedBodySize.get() > Integer.MAX_VALUE)
            ? monoError(logger, new IllegalStateException(
                "Response with body size " + cachedBodySize.get() + " doesn't fit into a String."))
            : getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }
}
