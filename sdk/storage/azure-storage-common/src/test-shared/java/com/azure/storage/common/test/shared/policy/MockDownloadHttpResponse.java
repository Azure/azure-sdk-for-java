// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it. Because this type is just for BlobDownload, we don't need to accept a header type.
 */
public class MockDownloadHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final Flux<ByteBuffer> body;

    public MockDownloadHttpResponse(HttpResponse response, int statusCode, Flux<ByteBuffer> body) {
        super(response.getRequest());
        this.statusCode = statusCode;
        this.headers = response.getHeaders();
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
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return body;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.error(new IOException());
    }

    @Override
    public Mono<String> getBodyAsString() {
        return Mono.error(new IOException());
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.error(new IOException());
    }
}
