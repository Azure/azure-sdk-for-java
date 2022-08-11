// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * A JDK response where the response body has been buffered into memory.
 */
final class BufferedJdkHttpResponse extends JdkHttpResponseBase {
    private final byte[] body;

    BufferedJdkHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] body) {
        super(request, statusCode, headers);
        this.body = body;
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryData.fromBytes(body);
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Mono.fromSupplier(() -> ByteBuffer.wrap(body)).flux();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(body);
    }

    @Override
    public HttpResponse buffer() {
        return this; // This response is already buffered.
    }
}
