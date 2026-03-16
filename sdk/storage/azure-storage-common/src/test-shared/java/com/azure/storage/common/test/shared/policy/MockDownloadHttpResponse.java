// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it. Because this type is just for BlobDownload, we don't need to accept a header type.
 */
public class MockDownloadHttpResponse extends HttpResponse {
    private final HttpResponse originalResponse;
    private final int statusCode;
    private final HttpHeaders headers;
    private final Flux<ByteBuffer> body;

    public MockDownloadHttpResponse(HttpResponse response, int statusCode, Flux<ByteBuffer> body) {
        this(response, statusCode, response.getHeaders(), body);
    }

    public MockDownloadHttpResponse(HttpResponse response, int statusCode, HttpHeaders headers,
        Flux<ByteBuffer> body) {
        super(response.getRequest());
        this.originalResponse = response;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(String s) {
        return headers.getValue(s);
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
        // Always close the wrapped response when body consumption terminates.
        return Flux.using(() -> originalResponse, ignored -> body, HttpResponse::close);
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesInByteBufferStream(getBody());
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> new String(bytes, Charset.defaultCharset()));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    @Override
    public void close() {
        originalResponse.close();
    }
}
