// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import com.azure.storage.common.implementation.Constants;
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

    DecodedResponse(HttpResponse originalResponse, Flux<ByteBuffer> decodedBody) {
        super(originalResponse.getRequest());
        this.originalResponse = originalResponse;
        this.decodedBody = decodedBody;
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
        HttpHeaders headers = new HttpHeaders(originalResponse.getHeaders());
        String structuredContentLength
            = headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
        if (structuredContentLength != null) {
            headers.set(HttpHeaderName.CONTENT_LENGTH, structuredContentLength);
        } else {
            headers.remove(HttpHeaderName.CONTENT_LENGTH);
        }
        return headers;
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
}
