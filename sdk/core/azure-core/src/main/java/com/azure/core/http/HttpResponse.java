// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.http.BufferedHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * The response of an {@link HttpRequest}.
 */
public abstract class HttpResponse implements Closeable {
    private final HttpRequest request;

    /**
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     */
    protected HttpResponse(HttpRequest request) {
        this.request = request;
    }

    /**
     * Get the response status code.
     *
     * @return The response status code
     */
    public abstract int getStatusCode();

    /**
     * Lookup a response header with the provided name.
     *
     * @param name the name of the header to lookup.
     * @return the value of the header, or null if the header doesn't exist in the response.
     */
    public abstract String getHeaderValue(String name);

    /**
     * Get all response headers.
     *
     * @return the response headers
     */
    public abstract HttpHeaders getHeaders();

    /**
     * Get the publisher emitting response content chunks.
     * <p>
     * Returns a stream of the response's body content. Emissions may occur on Reactor threads which should not be
     * blocked. Blocking should be avoided as much as possible/practical in reactive programming but if you do use
     * methods like {@code block()} on the stream then be sure to use {@code publishOn} before the blocking call.
     *
     * @return The response's content as a stream of {@link ByteBuffer}.
     */
    public abstract Flux<ByteBuffer> getBody();

    /**
     * Gets the response content as a {@code byte[]}.
     *
     * @return The response content as a {@code byte[]}.
     */
    public abstract Mono<byte[]> getBodyAsByteArray();

    /**
     * Gets the response content as a {@link String}.
     * <p>
     * By default this method will inspect the response body for containing a byte order mark (BOM) to determine the
     * encoding of the string (UTF-8, UTF-16, etc.). If a BOM isn't found this will default to using UTF-8 as the
     * encoding, if a specific encoding is required use {@link #getBodyAsString(Charset)}.
     *
     * @return The response content as a {@link String}.
     */
    public abstract Mono<String> getBodyAsString();

    /**
     * Gets the response content as a {@link String}.
     *
     * @param charset The {@link Charset} to use as the string encoding.
     * @return The response content as a {@link String}.
     */
    public abstract Mono<String> getBodyAsString(Charset charset);

    /**
     * Gets the response content as an {@link InputStream}.
     *
     * @return The response content as an {@link InputStream}.
     */
    public Mono<InputStream> getBodyAsInputStream() {
        return getBodyAsByteArray().map(ByteArrayInputStream::new);
    }

    /**
     * Gets the {@link HttpRequest request} which resulted in this response.
     *
     * @return The {@link HttpRequest request} which resulted in this response.
     */
    public final HttpRequest getRequest() {
        return request;
    }

    /**
     * Gets a new {@link HttpResponse response} object wrapping this response with its content buffered into memory.
     *
     * @return A new {@link HttpResponse response} with the content buffered.
     */
    public HttpResponse buffer() {
        return new BufferedHttpResponse(this);
    }

    /**
     * Closes the response content stream, if any.
     */
    @Override
    public void close() {
    }
}
