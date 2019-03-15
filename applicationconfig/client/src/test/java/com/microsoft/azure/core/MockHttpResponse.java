// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.core;

import com.microsoft.rest.v3.http.HttpHeaders;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.implementation.serializer.SerializerAdapter;
import com.microsoft.rest.v3.implementation.serializer.SerializerEncoding;
import com.microsoft.rest.v3.implementation.serializer.jackson.JacksonAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

class MockHttpResponse extends HttpResponse {
    private final static SerializerAdapter serializer = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    MockHttpResponse(int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
    }

    MockHttpResponse(int statusCode, byte[] bodyBytes) {
        this(statusCode, new HttpHeaders(), bodyBytes);
    }

    MockHttpResponse(int statusCode) {
        this(statusCode, new byte[0]);
    }

    MockHttpResponse(int statusCode, HttpHeaders headers, Object serializable) {
        this(statusCode, headers, serialize(serializable));
    }

    private static byte[] serialize(Object serializable) {
        byte[] result = null;
        try {
            final String serializedString = serializer.serialize(serializable, SerializerEncoding.JSON);
            result = serializedString == null ? null : serializedString.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String headerValue(String name) {
        return headers.value(name);
    }

    @Override
    public HttpHeaders headers() {
        return new HttpHeaders(headers);
    }

    @Override
    public Mono<byte[]> bodyAsByteArray() {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(bodyBytes);
        }
    }

    @Override
    public Flux<ByteBuf> body() {
        if (bodyBytes == null) {
            return Flux.empty();
        } else {
            return Flux.just(Unpooled.wrappedBuffer(bodyBytes));
        }
    }

    @Override
    public Mono<String> bodyAsString() {
        return bodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<String> bodyAsString(Charset charset) {
        Objects.requireNonNull(charset);

        return bodyBytes == null
                ? Mono.empty()
                : Mono.just(new String(bodyBytes, charset));
    }
}
