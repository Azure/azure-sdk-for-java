// Copyright (c) Microsoft Corporation. All rights reserved.  
// Licensed under the MIT License.  

package com.azure.identity.implementation.customtokenproxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CustomTokenProxyHttpResponse extends HttpResponse {

    // private final HttpRequest request;
    private final int statusCode;
    private final HttpHeaders headers;
    private final HttpURLConnection connection;
    private byte[] cachedResponseBodyBytes;

    public CustomTokenProxyHttpResponse(HttpRequest request, HttpURLConnection connection) {
        super(request);
        this.connection = connection;
        this.statusCode = extractStatusCode(connection);
        this.headers = extractHeaders(connection);
    }

    private HttpHeaders extractHeaders(HttpURLConnection connection) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            String headerName = entry.getKey();
            if (headerName != null) {
                for (String headerValue : entry.getValue()) {
                    headers.add(headerName, headerValue);
                }
            }
        }
        return headers;
    }

    private int extractStatusCode(HttpURLConnection connection) {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get status code from token proxy response", e);
        }
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(String name) {
        return headers.getValue(HttpHeaderName.fromString(name));
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.fromCallable(() -> {
            if (cachedResponseBodyBytes != null) {
                return cachedResponseBodyBytes;
            }
            try (InputStream stream = getResponseStream()) {
                if (stream == null) {
                    cachedResponseBodyBytes = new byte[0];
                    return cachedResponseBodyBytes;
                }
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] tmp = new byte[4096];
                int n;
                while ((n = stream.read(tmp)) != -1) {
                    buffer.write(tmp, 0, n);
                }
                cachedResponseBodyBytes = buffer.toByteArray();
                return cachedResponseBodyBytes;
            }
        });
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return getBodyAsByteArray().flatMapMany(bytes -> Flux.just(ByteBuffer.wrap(bytes)));
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    @Override
    public void close() {
        connection.disconnect();
    }

    private InputStream getResponseStream() throws IOException {
        try {
            return connection.getInputStream();
        } catch (IOException e) {
            return connection.getErrorStream();
        }
    }

}
