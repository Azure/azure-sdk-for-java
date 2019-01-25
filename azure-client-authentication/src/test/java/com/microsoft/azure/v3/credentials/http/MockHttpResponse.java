/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v3.credentials.http;

import com.microsoft.rest.v3.http.HttpHeaders;
import com.microsoft.rest.v3.http.HttpResponse;
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
    public String headerValue(String headerName) {
        return headers.value(headerName);
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
    public Flux<ByteBuffer> body() {
        return Flux.just(ByteBuffer.wrap(byteArray));
    }

    @Override
    public Mono<String> bodyAsString() {
        return Mono.just(string);
    }
}
