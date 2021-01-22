// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class NullHttpResponse extends HttpResponse {
    NullHttpResponse(HttpRequest request) {
        super(request);
    }

    @Override
    public int getStatusCode() {
        return 204;
    }

    @Override
    public String getHeaderValue(String s) {
        return null;
    }

    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders();
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Flux.empty();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.empty();
    }

    @Override
    public Mono<String> getBodyAsString() {
        return Mono.empty();
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.empty();
    }
}
