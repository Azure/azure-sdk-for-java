// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/** HttpResponse wrapper for {@link com.azure.core.http.rest.Response} with FluxBB body. */
public class HttpFluxBBResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders httpHeaders;
    private final Response<Flux<ByteBuffer>> originResponse;

    HttpFluxBBResponse(Response<Flux<ByteBuffer>> originResponse) {
        super(null);
        this.statusCode = originResponse.getStatusCode();
        this.httpHeaders = originResponse.getHeaders();
        this.originResponse = originResponse;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(String s) {
        return httpHeaders.getValue(s);
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return originResponse.getValue();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return FluxUtil.collectBytesInByteBufferStream(originResponse.getValue());
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(String::new);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }
}
