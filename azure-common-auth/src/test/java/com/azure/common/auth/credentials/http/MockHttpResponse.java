/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials.http;

import com.azure.common.http.HttpHeaders;
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

public class MockHttpResponse extends HttpResponse {
    private final static SerializerAdapter serializer = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private byte[] byteArray;
    private String string;

    public MockHttpResponse(int statusCode) {
        this.statusCode = statusCode;

        headers = new HttpHeaders();
    }

    public MockHttpResponse(int statusCode, byte[] byteArray) {
        this(statusCode);

        this.byteArray = byteArray;
    }

    public MockHttpResponse(int statusCode, String string) {
        this(statusCode);

        this.string = string;
    }

    public MockHttpResponse(int statusCode, Object serializable) {
        this(statusCode);

        try {
            this.string = serializer.serialize(serializable, SerializerEncoding.JSON);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return Mono.just(byteArray);
    }

    @Override
    public Flux<ByteBuf> body() {
        return Flux.just(Unpooled.wrappedBuffer(byteArray));
    }

    @Override
    public Mono<String> bodyAsString() {
        return Mono.just(string);
    }

    @Override
    public Mono<String> bodyAsString(Charset charset) {
        return Mono.just(string);
    }
}
