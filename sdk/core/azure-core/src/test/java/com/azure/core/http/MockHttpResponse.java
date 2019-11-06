// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.JacksonAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MockHttpResponse extends HttpResponse {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
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
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(String name) {
        return headers.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders(headers);
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(bodyBytes);
        }
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        if (bodyBytes == null) {
            return Flux.empty();
        } else {
            return Flux.just(ByteBuffer.wrap(bodyBytes));
        }
    }

    @Override
    public Mono<String> getBodyAsString() {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(new String(bodyBytes, StandardCharsets.UTF_8));
        }
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(new String(bodyBytes, charset));
        }
    }
}
