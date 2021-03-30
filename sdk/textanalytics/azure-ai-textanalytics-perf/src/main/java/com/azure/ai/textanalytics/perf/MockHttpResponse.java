// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.perf;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A mock http response used for local perf testing.
 */
public class MockHttpResponse extends HttpResponse {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    /**
     * Creates an instance of mock response.
     * @param request The HTTP request.
     * @param statusCode The HTTP resposne status code.
     * @param headers The HTTP response headers.
     * @param bodyBytes The HTTP response body.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
    }

    /**
     * Creates an instance of mock response.
     * @param request The HTTP request.
     * @param statusCode The HTTP resposne status code.
     */
    public MockHttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, new HttpHeaders(), new byte[0]);
    }


    /**
     * Creates an instance of mock response.
     * @param request The HTTP request.
     * @param statusCode The HTTP resposne status code.
     * @param headers The HTTP response headers.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
        this(request, statusCode, headers, new byte[0]);
    }

    /**
     * Creates an instance of mock response.
     * @param request The HTTP request.
     * @param statusCode The HTTP resposne status code.
     * @param headers The HTTP response headers.
     * @param serializable The HTTP response body serializable object.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, Object serializable) {
        this(request, statusCode, headers, serialize(serializable));
    }


    /**
     * Creates an instance of mock response.
     * @param request The HTTP request.
     * @param statusCode The HTTP resposne status code.
     * @param serializable The HTTP response body serializable object.
     */
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
    public String getHeaderValue(String name) {
        return headers.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders(headers);
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
}
