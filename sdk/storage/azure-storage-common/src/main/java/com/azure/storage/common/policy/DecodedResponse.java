// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Decoded HTTP response that wraps the original response with a decoded body stream.
 */
class DecodedResponse extends HttpResponse {
    private final HttpResponse originalResponse;
    private final Flux<ByteBuffer> decodedBody;
    private final DecoderState decoderState;

    DecodedResponse(HttpResponse originalResponse, Flux<ByteBuffer> decodedBody, DecoderState decoderState) {
        super(originalResponse.getRequest());
        this.originalResponse = originalResponse;
        this.decodedBody = decodedBody;
        this.decoderState = decoderState;
    }

    @Override
    public int getStatusCode() {
        return originalResponse.getStatusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return originalResponse.getHeaderValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return originalResponse.getHeaders();
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Flux.using(() -> originalResponse, r -> decodedBody, HttpResponse::close);
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesInByteBufferStream(getBody());
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> new String(bytes, Charset.defaultCharset()));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    @Override
    public void close() {
        originalResponse.close();
    }

    DecoderState getDecoderState() {
        return decoderState;
    }
}
