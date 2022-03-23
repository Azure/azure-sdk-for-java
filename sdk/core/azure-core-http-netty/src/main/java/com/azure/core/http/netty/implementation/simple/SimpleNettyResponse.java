// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class SimpleNettyResponse extends HttpResponse {

    private final BinaryData content;
    private final int statusCode;
    private final HttpHeaders httpHeaders;

    public SimpleNettyResponse(SimpleRequestContext requestContext) {
        super(requestContext.getRequest());
        statusCode = requestContext.getStatusCode();
        httpHeaders = requestContext.getHttpHeaders();
        this.content = requestContext.getBodyCollector().toBinaryData();
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(String name) {
        return httpHeaders.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }

    @Override
    public BinaryData getContent() {
        return content;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        if (content == null) {
            return Flux.empty();
        }
        return content.toFluxByteBuffer();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        if (content == null) {
            return Mono.empty();
        }
        return Mono.fromSupplier(content::toBytes);
    }

    @Override
    public Mono<String> getBodyAsString() {
        if (content == null) {
            return Mono.empty();
        }
        return getBodyAsByteArray().map(
            bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue("Content-Type")));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        if (content == null) {
            return Mono.empty();
        }
        return Mono.fromSupplier(() -> new String(content.toBytes(), charset));
    }
}
