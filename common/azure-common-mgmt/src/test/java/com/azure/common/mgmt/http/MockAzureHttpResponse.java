/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt.http;

import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
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

public class MockAzureHttpResponse extends HttpResponse {
    private final static SerializerAdapter serializer = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    public MockAzureHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        this.headers = headers;

        this.statusCode = statusCode;
        this.bodyBytes = bodyBytes;
        this.withRequest(request);
    }

    public MockAzureHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
        this(request, statusCode, headers, new byte[0]);
    }

    public MockAzureHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, String string) {
        this(request, statusCode, headers, string == null ? new byte[0] : string.getBytes());
    }

    public MockAzureHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, Object serializable) {
        this(request, statusCode, headers, serialize(serializable));
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
        return Mono.just(bodyBytes);
    }

    @Override
    public Flux<ByteBuf> body() {
        return Flux.just(Unpooled.wrappedBuffer(bodyBytes));
    }

    @Override
    public Mono<String> bodyAsString() {
        return Mono.just(new String(bodyBytes, StandardCharsets.UTF_8));
    }

    @Override
    public Mono<String> bodyAsString(Charset charset) {
        return Mono.just(new String(bodyBytes, charset));
    }

    public MockAzureHttpResponse withHeader(String headerName, String headerValue) {
        headers.set(headerName, headerValue);
        return this;
    }
}
