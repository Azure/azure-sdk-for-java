// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * The outgoing Http request.
 */
public class HttpRequest {
    private HttpMethod httpMethod;
    private URI uri;
    private String uriString;
    private int port;
    private HttpHeaders headers;
    private Flux<byte[]> body;
    private ReactorNettyRequestRecord reactorNettyRequestRecord;
    private boolean isThinClientRequest;

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param uri        the target address to send the request to
     */
    public HttpRequest(HttpMethod httpMethod, URI uri, int port, HttpHeaders httpHeaders) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.uriString = uri.toString();
        this.port = port;
        this.headers = httpHeaders;
        this.reactorNettyRequestRecord = createReactorNettyRequestRecord();
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param uri        the target address to send the request to
     */
    public HttpRequest(HttpMethod httpMethod, String uri, int port) throws URISyntaxException {
        this.httpMethod = httpMethod;
        this.uriString = uri;
        this.uri = null;
        this.port = port;
        this.headers = new HttpHeaders();
        this.reactorNettyRequestRecord = createReactorNettyRequestRecord();
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param uri        the target address to send the request to
     * @param headers    the HTTP headers to use with this request
     * @param body       the request content
     */
    public HttpRequest(HttpMethod httpMethod, URI uri, int port, HttpHeaders headers, Flux<byte[]> body) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.uriString = uri.toString();
        this.port = port;
        this.headers = headers;
        this.body = body;
        this.reactorNettyRequestRecord = createReactorNettyRequestRecord();
    }

    /**
     * Create a new HttpRequest instance from a URI string without parsing it.
     *
     * @param httpMethod the HTTP request method
     * @param uriString  the target address as a string (URI parsing is deferred)
     * @param port       the target port
     * @param headers    the HTTP headers to use with this request
     * @param body       the request content
     */
    public HttpRequest(HttpMethod httpMethod, String uriString, int port, HttpHeaders headers, Flux<byte[]> body) {
        this.httpMethod = httpMethod;
        this.uriString = uriString;
        this.uri = null;
        this.port = port;
        this.headers = headers;
        this.body = body;
        this.reactorNettyRequestRecord = createReactorNettyRequestRecord();
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
        URI result = this.uri;
        if (result == null) {
            try {
                result = new URI(this.uriString);
                this.uri = result;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI: " + this.uriString, e);
            }
        }
        return result;
    }

    /**
     * Get the target address as a string without triggering URI parsing.
     *
     * @return the target address string
     */
    public String uriString() {
        return this.uriString;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param uri target address as {@link URI}
     * @return this HttpRequest
     */
    public HttpRequest withUri(URI uri) {
        this.uri = uri;
        this.uriString = uri.toString();
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
    public Flux<byte[]> body() {
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
        return withBody(Flux.just(bodyBytes));
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
        this.body = Flux.just(content);
        return this;
    }

    /**
     * Set the request content.
     * The Content-Length header will be set based on the given content's length
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest withBody(Flux<byte[]> content) {
        this.body = content;
        return this;
    }

    /**
     * Sets ReactorNettyRequestRecord for recording request timeline.
     *
     * @param reactorNettyRequestRecord the reactor netty request record
     * @return this HttpRequest
     */
    public HttpRequest withReactorNettyRequestRecord(ReactorNettyRequestRecord reactorNettyRequestRecord) {
        this.reactorNettyRequestRecord = reactorNettyRequestRecord;
        return this;
    }

    /**
     * Gets whether this request targets the thin client proxy.
     * Set by {@link com.azure.cosmos.implementation.ThinClientStoreModel} during request construction.
     *
     * @return true if this is a thin client request, false for standard gateway requests
     */
    public boolean isThinClientRequest() {
        return this.isThinClientRequest;
    }

    /**
     * Marks this request as targeting the thin client proxy.
     * This is used to apply thin-client-specific transport settings (e.g., connect timeout).
     *
     * @param isThinClientRequest true if this request targets the thin client proxy
     * @return this HttpRequest
     */
    public HttpRequest withThinClientRequest(boolean isThinClientRequest) {
        this.isThinClientRequest = isThinClientRequest;
        return this;
    }

    /**
     * Gets ReactorNettyRequestRecord for recording request timeline
     *
     * @return reactorNettyRequestRecord the reactor netty request record
     */
    public ReactorNettyRequestRecord reactorNettyRequestRecord() {
        return this.reactorNettyRequestRecord;
    }

    private ReactorNettyRequestRecord createReactorNettyRequestRecord(){
        ReactorNettyRequestRecord reactorNettyRequestRecord = new ReactorNettyRequestRecord();
        reactorNettyRequestRecord.setTimeCreated(Instant.now());
        return reactorNettyRequestRecord;
    }
}
