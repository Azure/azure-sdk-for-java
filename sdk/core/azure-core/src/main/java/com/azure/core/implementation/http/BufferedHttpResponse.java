// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final HttpResponse innerHttpResponse;
    private final BinaryData cachedBody;

    /**
     * Creates a buffered HTTP response.
     *
     * @param innerHttpResponse The HTTP response to buffer
     * @param bufferedResponseContent The buffered response content.
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse, BinaryData bufferedResponseContent) {
        super(innerHttpResponse.getRequest());
        this.innerHttpResponse = innerHttpResponse;
        innerHttpResponse.close(); // Close the inner response since we've cached the response body.
        this.cachedBody = bufferedResponseContent;
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
        return cachedBody.toFluxByteBuffer();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        // Check that the body would fit into a byte array before spending time to create the merged byte array.
        return Mono.fromCallable(cachedBody::toBytes);
    }

    @Override
    public Mono<String> getBodyAsString() {
        // Check that the body would fit into a String before spending the time to create the String.
        return Mono.fromCallable(cachedBody::toString);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        // Check that the body would fit into a String before spending the time to create the String.
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return cachedBody;
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        return Mono.fromCallable(cachedBody::toStream);
    }

    @Override
    public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
        return cachedBody.writeTo(channel);
    }

    @Override
    public void writeBodyTo(WritableByteChannel channel) throws IOException {
        cachedBody.writeTo(channel);
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }

    @Override
    public Mono<HttpResponse> bufferAsync() {
        return Mono.just(this);
    }

    @Override
    public boolean isBuffered() {
        return true;
    }
}
