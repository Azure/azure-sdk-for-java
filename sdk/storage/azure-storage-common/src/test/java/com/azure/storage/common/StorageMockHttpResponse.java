// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * An HTTP response that is created to simulate a HTTP request.
 */
public final class StorageMockHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final Flux<ByteBuffer> body;

    /**
     * Creates a HTTP response associated with a {@code request}, returns the {@code statusCode}, and has an empty
     * response body.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     */
    public StorageMockHttpResponse(HttpRequest request, int statusCode) {
        super(request);
        this.statusCode = statusCode;
        this.headers = new HttpHeaders();
        this.body = Flux.empty();
    }

    /**
     * Creates a HTTP response from the passed {@code response} changing the {@code statusCode} and {@code body} that
     * is returned.
     *
     * @param response HttpResponse used to construct the mock response.
     * @param statusCode Status code of the response.
     * @param body Body of the response.
     */
    public StorageMockHttpResponse(HttpResponse response, int statusCode, Flux<ByteBuffer> body) {
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
    public String getHeaderValue(String name) {
        return headers.getValue(name);
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
        return FluxUtil.collectBytesInByteBufferStream(body);
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
