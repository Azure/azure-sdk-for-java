// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.http;

import com.azure.core.v2.util.FluxUtil;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MockFluxHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final Flux<ByteBuffer> bodyBytes;

    private boolean closed;

    public MockFluxHttpResponse(HttpRequest request, Flux<ByteBuffer> bodyBytes) {
        super(request);
        this.statusCode = 200;
        this.headers = new HttpHeaders();
        this.bodyBytes = bodyBytes;
    }

    public MockFluxHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> bodyBytes) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
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
        return this.headers;
    }

    @Override
    public byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesInByteBufferStream(bodyBytes);
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return bodyBytes;
    }

    @Override
    public String> getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    @Override
    public void close() {
        closed = true;
        super.close();
    }

    public boolean isClosed() {
        return closed;
    }
}
