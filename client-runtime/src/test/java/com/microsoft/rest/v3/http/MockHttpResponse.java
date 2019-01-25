/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

import com.microsoft.rest.v3.protocol.SerializerAdapter;
import com.microsoft.rest.v3.protocol.SerializerEncoding;
import com.microsoft.rest.v3.serializer.JacksonAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MockHttpResponse extends HttpResponse {
    private final static SerializerAdapter<?> serializer = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    public MockHttpResponse(int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
    }

    public MockHttpResponse(int statusCode, byte[] bodyBytes) {
        this(statusCode, new HttpHeaders(), bodyBytes);
    }

    public MockHttpResponse(int statusCode) {
        this(statusCode, new byte[0]);
    }

    public MockHttpResponse(int statusCode, String string) {
        this(statusCode, new HttpHeaders(), string == null ? new byte[0] : string.getBytes());
    }

    public MockHttpResponse(int statusCode, HttpHeaders headers) {
        this(statusCode, headers, new byte[0]);
    }

    public MockHttpResponse(int statusCode, HttpHeaders headers, Object serializable) {
        this(statusCode, headers, serialize(serializable));
    }

    public MockHttpResponse(int statusCode, Object serializable) {
        this(statusCode, new HttpHeaders(), serialize(serializable));
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
    public String headerValue(String headerName) {
        return headers.value(headerName);
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
    public Flux<ByteBuffer> body() {
        if (bodyBytes == null) {
            return Flux.empty();
        } else {
            return Flux.just(ByteBuffer.wrap(bodyBytes));
        }
    }

    @Override
    public Mono<String> bodyAsString() {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(new String(bodyBytes));
        }
    }
}
