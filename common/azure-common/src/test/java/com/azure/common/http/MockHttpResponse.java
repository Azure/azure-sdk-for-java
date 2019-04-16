// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http;

import com.azure.common.implementation.serializer.SerializerAdapter;
import com.azure.common.implementation.serializer.SerializerEncoding;
import com.azure.common.implementation.serializer.jackson.JacksonAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MockHttpResponse extends HttpResponse {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
        this.withRequest(request);
    }

    public MockHttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, new HttpHeaders(), new byte[0]);
    }

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
        this(request, statusCode, headers, new byte[0]);
    }

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, Object serializable) {
        this(request, statusCode, headers, serialize(serializable));
    }

    public MockHttpResponse(HttpRequest request, int statusCode, Object serializable) {
        this(request, statusCode, new HttpHeaders(), serialize(serializable));
    }

    private static byte[] serialize(Object serializable) {
        byte[] result = null;
        try {
            final String serializedString = SERIALIZER.serialize(serializable, SerializerEncoding.JSON);
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
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(new String(bodyBytes, StandardCharsets.UTF_8));
        }
    }

    @Override
    public Mono<String> bodyAsString(Charset charset) {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(new String(bodyBytes, charset));
        }
    }
}
