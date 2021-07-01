// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * An HTTP response that is created to simulate a HTTP request.
 */
public class MockHttpResponse extends HttpResponse {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    /**
     * Creates a HTTP response associated with a {@code request}, returns the {@code statusCode}, and has an empty
     * response body.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, new byte[0]);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and response body of
     * {@code bodyBytes}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param bodyBytes Contents of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, byte[] bodyBytes) {
        this(request, statusCode, new HttpHeaders(), bodyBytes);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and http headers.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param headers Headers of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
        this(request, statusCode, headers, new byte[0]);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, contains the
     * {@code headers}, and response body of {@code bodyBytes}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param headers HttpHeaders of the response.
     * @param bodyBytes Contents of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = CoreUtils.clone(bodyBytes);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, contains the given
     * {@code headers}, and response body that is JSON serialized from {@code serializable}.
     *
     * @param request HttpRequest associated with the response.
     * @param headers HttpHeaders of the response.
     * @param statusCode Status code of the response.
     * @param serializable Contents to be serialized into JSON for the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, Object serializable) {
        this(request, statusCode, headers, serialize(serializable));
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and response body
     * that is JSON serialized from {@code serializable}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param serializable Contents to be serialized into JSON for the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, Object serializable) {
        this(request, statusCode, new HttpHeaders(), serialize(serializable));
    }

    private static byte[] serialize(Object serializable) {
        byte[] result = null;
        try {
            final String serializedString = SERIALIZER.serialize(serializable, SerializerEncoding.JSON);
            result = serializedString == null ? null : serializedString.getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeaderValue(String name) {
        return headers.getValue(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders(headers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        if (bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(bodyBytes);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<ByteBuffer> getBody() {
        if (bodyBytes == null) {
            return Flux.empty();
        } else {
            return Flux.just(ByteBuffer.wrap(bodyBytes));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<String> getBodyAsString() {
        return (bodyBytes == null)
            ? Mono.empty()
            : Mono.just(CoreUtils.bomAwareToString(bodyBytes, getHeaderValue("Content-Type")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        Objects.requireNonNull(charset, "'charset' cannot be null.");

        return bodyBytes == null
                ? Mono.empty()
                : Mono.just(new String(bodyBytes, charset));
    }

    /**
     * Adds the header {@code name} and {@code value} to the existing set of HTTP headers.
     * @param name The header to add
     * @param value The header value.
     * @return The updated response object.
     */
    public MockHttpResponse addHeader(String name, String value) {
        headers.set(name, value);
        return this;
    }

    @Override
    public HttpResponse buffer() {
        return this; // This response is already buffered.
    }
}
