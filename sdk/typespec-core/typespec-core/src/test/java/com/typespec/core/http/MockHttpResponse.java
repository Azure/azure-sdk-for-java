// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http;

import com.typespec.core.util.BinaryData;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.serializer.SerializerAdapter;
import com.typespec.core.util.serializer.SerializerEncoding;
import com.typespec.core.util.serializer.JacksonAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MockHttpResponse extends HttpResponse {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
    }

    public MockHttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, new HttpHeaders(), new byte[0]);
    }

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
        this(request, statusCode, headers, new byte[0]);
    }

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, Object serializable) {
        this(request, statusCode, headers, serialize(serializable));
    }

    public MockHttpResponse(HttpRequest request, int statusCode, Object serializable) {
        this(request, statusCode, new HttpHeaders(), serialize(serializable));
    }

    private static byte[] serialize(Object serializable) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            SERIALIZER.serialize(serializable, SerializerEncoding.JSON, stream);

            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    @Deprecated
    public String getHeaderValue(String name) {
        return headers.getValue(name);
    }

    @Override
    public String getHeaderValue(HttpHeaderName headerName) {
        return headers.getValue(headerName);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(bodyBytes);
        }
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        if (bodyBytes == null) {
            return Flux.empty();
        } else {
            return Flux.just(ByteBuffer.wrap(bodyBytes));
        }
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(new String(bodyBytes, charset));
        }
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryData.fromBytes(bodyBytes);
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        return getBodyAsByteArray().map(ByteArrayInputStream::new);
    }

    @Override
    public HttpResponse buffer() {
        return this;
    }

    @Override
    public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
        return FluxUtil.writeToAsynchronousByteChannel(getBody(), channel);
    }

    @Override
    public void writeBodyTo(WritableByteChannel channel) throws IOException {
        FluxUtil.writeToWritableByteChannel(getBody(), channel).block();
    }
}
