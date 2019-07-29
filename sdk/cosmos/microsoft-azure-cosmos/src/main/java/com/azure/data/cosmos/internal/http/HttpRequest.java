// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * The outgoing Http request.
 */
public class HttpRequest {
    private HttpMethod httpMethod;
    private URI uri;
    private int port;
    private HttpHeaders headers;
    private Flux<ByteBuf> body;

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param uri        the target address to send the request to
     */
    public HttpRequest(HttpMethod httpMethod, URI uri, int port, HttpHeaders httpHeaders) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.port = port;
        this.headers = httpHeaders;
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param uri        the target address to send the request to
     */
    public HttpRequest(HttpMethod httpMethod, String uri, int port) throws URISyntaxException {
        this.httpMethod = httpMethod;
        this.uri = new URI(uri);
        this.port = port;
        this.headers = new HttpHeaders();
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param uri        the target address to send the request to
     * @param headers    the HTTP headers to use with this request
     * @param body       the request content
     */
    public HttpRequest(HttpMethod httpMethod, URI uri, int port, HttpHeaders headers, Flux<ByteBuf> body) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.port = port;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Get the request method.
     *
     * @return the request method
     */
    public HttpMethod httpMethod() {
        return httpMethod;
    }

    /**
     * Set the request method.
     *
     * @param httpMethod the request method
     * @return this HttpRequest
     */
    public HttpRequest withHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Get the target port.
     *
     * @return the target port
     */
    public int port() {
        return port;
    }

    /**
     * Set the target port to send the request to.
     *
     * @param port target port
     * @return this HttpRequest
     */
    public HttpRequest withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Get the target address.
     *
     * @return the target address
     */
    public URI uri() {
        return uri;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param uri target address as {@link URI}
     * @return this HttpRequest
     */
    public HttpRequest withUri(URI uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Get the request headers.
     *
     * @return headers to be sent
     */
    public HttpHeaders headers() {
        return headers;
    }

    /**
     * Set the request headers.
     *
     * @param headers the set of headers
     * @return this HttpRequest
     */
    public HttpRequest withHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Set a request header, replacing any existing value.
     * A null for {@code value} will remove the header if one with matching name exists.
     *
     * @param name  the header name
     * @param value the header value
     * @return this HttpRequest
     */
    public HttpRequest withHeader(String name, String value) {
        headers.set(name, value);
        return this;
    }

    /**
     * Get the request content.
     *
     * @return the content to be send
     */
    public Flux<ByteBuf> body() {
        return body;
    }

    /**
     * Set the request content.
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest withBody(String content) {
        final byte[] bodyBytes = content.getBytes(StandardCharsets.UTF_8);
        return withBody(bodyBytes);
    }

    /**
     * Set the request content.
     * The Content-Length header will be set based on the given content's length
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest withBody(byte[] content) {
        headers.set("Content-Length", String.valueOf(content.length));
        // Unpooled.wrappedBuffer(body) allocates ByteBuf from unpooled heap
        return withBody(Flux.defer(() -> Flux.just(Unpooled.wrappedBuffer(content))));
    }

    /**
     * Set request content.
     * <p>
     * Caller must set the Content-Length header to indicate the length of the content,
     * or use Transfer-Encoding: chunked.
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest withBody(Flux<ByteBuf> content) {
        this.body = content;
        return this;
    }
}
