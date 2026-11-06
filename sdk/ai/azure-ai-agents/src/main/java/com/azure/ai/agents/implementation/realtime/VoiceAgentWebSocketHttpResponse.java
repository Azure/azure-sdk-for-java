// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import okhttp3.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * Azure Core response adapter for a rejected WebSocket handshake.
 */
public final class VoiceAgentWebSocketHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final byte[] body;

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
        this.body = response instanceof FullHttpResponse
            ? ByteBufUtil.getBytes(((FullHttpResponse) response).content())
            : new byte[0];
    }

    /**
     * Creates a response adapter.
     *
     * @param endpoint the WebSocket endpoint.
     * @param response the rejected OkHttp handshake response.
     */
    public VoiceAgentWebSocketHttpResponse(URI endpoint, okhttp3.Response response) {
        super(new HttpRequest(HttpMethod.GET, toHttpUrl(endpoint)));
        this.statusCode = response.code();
        this.headers = new HttpHeaders();
        for (String name : response.headers().names()) {
            for (String value : response.headers(name)) {
                this.headers.add(HttpHeaderName.fromString(name), value);
            }
        }
        this.body = readBody(response.body());
    }

    private static byte[] readBody(ResponseBody responseBody) {
        if (responseBody == null) {
            return new byte[0];
        }
        try {
            return responseBody.bytes();
        } catch (IOException error) {
            throw new UncheckedIOException("Failed to read the WebSocket handshake response body.", error);
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
        return body.length == 0 ? Flux.empty() : Flux.just(ByteBuffer.wrap(Arrays.copyOf(body, body.length)));
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(Arrays.copyOf(body, body.length));
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.just(new String(body, charset));
    }
}
