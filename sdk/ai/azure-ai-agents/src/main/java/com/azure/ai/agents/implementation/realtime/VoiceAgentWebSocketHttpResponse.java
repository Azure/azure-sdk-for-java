// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Azure Core response adapter for a rejected WebSocket handshake.
 */
public final class VoiceAgentWebSocketHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;

    /**
     * Creates a response adapter.
     *
     * @param endpoint the WebSocket endpoint.
     * @param response the rejected Netty handshake response.
     */
    public VoiceAgentWebSocketHttpResponse(URI endpoint, io.netty.handler.codec.http.HttpResponse response) {
        super(new HttpRequest(HttpMethod.GET, toHttpUrl(endpoint)));
        this.statusCode = response.status().code();
        this.headers = new HttpHeaders();
        for (Map.Entry<String, String> header : response.headers()) {
            this.headers.set(com.azure.core.http.HttpHeaderName.fromString(header.getKey()), header.getValue());
        }
    }

    private static String toHttpUrl(URI endpoint) {
        String endpointUrl = endpoint.toString();
        if ("wss".equalsIgnoreCase(endpoint.getScheme())) {
            return "https" + endpointUrl.substring(endpoint.getScheme().length());
        }
        if ("ws".equalsIgnoreCase(endpoint.getScheme())) {
            return "http" + endpointUrl.substring(endpoint.getScheme().length());
        }
        return endpointUrl;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getHeaderValue(String name) {
        return headers.getValue(HttpHeaderName.fromString(name));
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Flux.empty();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(new byte[0]);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return Mono.just("");
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.just("");
    }
}
