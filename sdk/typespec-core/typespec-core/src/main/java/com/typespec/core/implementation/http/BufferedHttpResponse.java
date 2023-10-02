// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http;

import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.implementation.util.IterableOfByteBuffersInputStream;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.typespec.core.util.FluxUtil.monoError;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 */
public final class BufferedHttpResponse extends HttpResponse {
    // BufferedHttpResponse is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(BufferedHttpResponse.class);

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
    @Deprecated
    public String getHeaderValue(String name) {
        return innerHttpResponse.getHeaderValue(name);
    }

    @Override
    public String getHeaderValue(HttpHeaderName headerName) {
        return innerHttpResponse.getHeaderValue(headerName);
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
            ? monoError(LOGGER, new IllegalStateException(
                "Response with body size " + cachedBodySize.get() + " doesn't fit into a byte array."))
            : FluxUtil.collectBytesInByteBufferStream(getBody(), (int) cachedBodySize.get());
    }

    @Override
    public Mono<String> getBodyAsString() {
        // Check that the body would fit into a String before spending the time to create the String.
        return (cachedBodySize.get() > Integer.MAX_VALUE)
            ? monoError(LOGGER, new IllegalStateException(
                "Response with body size " + cachedBodySize.get() + " doesn't fit into a String."))
            : getBodyAsByteArray().map(bytes ->
                CoreUtils.bomAwareToString(bytes, innerHttpResponse.getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        // Check that the body would fit into a String before spending the time to create the String.
        return (cachedBodySize.get() > Integer.MAX_VALUE)
            ? monoError(LOGGER, new IllegalStateException(
                "Response with body size " + cachedBodySize.get() + " doesn't fit into a String."))
            : getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryData.fromFlux(getBody(), cachedBodySize.get(), false).block();
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        return getBody().collectList().map(IterableOfByteBuffersInputStream::new);
    }

    @Override
    public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
        return FluxUtil.writeToAsynchronousByteChannel(getBody(), channel);
    }

    @Override
    public void writeBodyTo(WritableByteChannel channel) throws IOException {
        FluxUtil.writeToWritableByteChannel(getBody(), channel).block();
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }
}
