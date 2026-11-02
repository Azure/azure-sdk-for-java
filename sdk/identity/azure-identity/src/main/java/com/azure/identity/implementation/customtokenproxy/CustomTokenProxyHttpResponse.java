// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public final class CustomTokenProxyHttpResponse extends HttpResponse {

    private static final ClientLogger LOGGER = new ClientLogger(CustomTokenProxyHttpResponse.class);

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
                    headers.add(HttpHeaderName.fromString(headerName), headerValue);
                }
            }
        }
        return headers;
    }

    private int extractStatusCode(HttpURLConnection connection) {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            throw LOGGER
                .logExceptionAsError(new RuntimeException("Failed to get status code from token proxy response", e));
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
        return Mono.fromCallable(this::readResponseBody).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        try {
            return BinaryData.fromBytes(readResponseBody());
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException("Failed to read token proxy response body.", e));
        }
    }

    @Override
    public InputStream getBodyAsInputStreamSync() {
        return getBodyAsBinaryData().toStream();
    }

    @Override
    public void writeBodyTo(WritableByteChannel channel) throws IOException {
        Objects.requireNonNull(channel, "'channel' must not be null.");
        try {
            BinaryData.fromBytes(readResponseBody()).writeTo(channel);
        } finally {
            close();
        }
    }

    private synchronized byte[] readResponseBody() throws IOException {
        if (cachedResponseBodyBytes != null) {
            return cachedResponseBodyBytes;
        }

        try (InputStream stream = getResponseStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int n;
            byte[] temp = new byte[4096];
            while ((n = stream.read(temp)) != -1) {
                buffer.write(temp, 0, n);
            }
            cachedResponseBodyBytes = buffer.toByteArray();
            return cachedResponseBodyBytes;
        }
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
        if (statusCode < 400) {
            // JDK getInputStream() throws for HTTP errors, so we only check for non-error status codes
            return connection.getInputStream();
        }
        InputStream errorStream = connection.getErrorStream();
        return errorStream == null ? new ByteArrayInputStream(new byte[0]) : errorStream;
    }

}
