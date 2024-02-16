// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A mock {@link HttpResponse} for testing.
 */
public final class MockHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final byte[] body;

    public MockHttpResponse(HttpRequest request, int statusCode, byte[] body, SerializerEncoding encoding) {
        super(request);

        this.statusCode = statusCode;
        this.headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE,
            encoding == SerializerEncoding.XML ? "application/xml" : "application/json");
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    @Deprecated
    public String getHeaderValue(String name) {
        return headers.getValue(name);
    }

    @Override
    public String getHeaderValue(HttpHeaderName headerName) {
        return headers.getValue(headerName);
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return getBodyAsByteArray().map(ByteBuffer::wrap).flux();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.justOrEmpty(body);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }
}
