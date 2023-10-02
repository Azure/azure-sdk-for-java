// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http;

import com.typespec.core.implementation.ImplUtils;
import com.typespec.core.implementation.util.BinaryDataHelper;
import com.typespec.core.implementation.util.FluxByteBufferContent;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * The outgoing Http request. It provides ways to construct {@link HttpRequest} with {@link HttpMethod}, {@link URL},
 * {@link HttpHeader} and request body.
 */
public class HttpRequest {
    // HttpRequest is a highly used, short-lived class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpRequest.class);

    private HttpMethod httpMethod;
    private URL url;
    private HttpHeaders headers;
    private BinaryData body;

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url the target address to send the request to
     */
    public HttpRequest(HttpMethod httpMethod, URL url) {
        this(httpMethod, url, new HttpHeaders(), (BinaryData) null);
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url the target address to send the request to
     * @throws IllegalArgumentException if {@code url} is null or it cannot be parsed into a valid URL.
     */
    public HttpRequest(HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;
        setUrl(url);
        this.headers = new HttpHeaders();
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url the target address to send the request to
     * @param headers the HTTP headers to use with this request
     */
    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = headers;
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url the target address to send the request to
     * @param headers the HTTP headers to use with this request
     * @param body the request content
     */
    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, Flux<ByteBuffer> body) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = headers;
        setBody(BinaryDataHelper.createBinaryData(new FluxByteBufferContent(body)));
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url the target address to send the request to
     * @param headers the HTTP headers to use with this request
     * @param body the request content
     */
    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, BinaryData body) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = headers;
        setBody(body);
    }

    /**
     * Get the request method.
     *
     * @return the request method
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Set the request method.
     *
     * @param httpMethod the request method
     * @return this HttpRequest
     */
    public HttpRequest setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Get the target address.
     *
     * @return the target address
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url target address as {@link URL}
     * @return this HttpRequest
     */
    public HttpRequest setUrl(URL url) {
        this.url = url;
        return this;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url target address as a String
     * @return this HttpRequest
     * @throws IllegalArgumentException if {@code url} is null or it cannot be parsed into a valid URL.
     */
    public HttpRequest setUrl(String url) {
        try {
            this.url = ImplUtils.createUrl(url);
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL.", ex));
        }
        return this;
    }

    /**
     * Get the request headers.
     *
     * @return headers to be sent
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Set the request headers.
     *
     * @param headers the set of headers
     * @return this HttpRequest
     */
    public HttpRequest setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Set a request header, replacing any existing value. A null for {@code value} will remove the header if one with
     * matching name exists.
     *
     * @param name the header name
     * @param value the header value
     * @return this HttpRequest
     * @deprecated Use {@link #setHeader(HttpHeaderName, String)} instead as is offers better performance.
     */
    @Deprecated
    public HttpRequest setHeader(String name, String value) {
        headers.set(name, value);
        return this;
    }

    /**
     * Set a request header, replacing any existing value. A null for {@code value} will remove the header if one with
     * matching name exists.
     *
     * @param headerName the header name
     * @param value the header value
     * @return this HttpRequest
     */
    public HttpRequest setHeader(HttpHeaderName headerName, String value) {
        headers.set(headerName, value);
        return this;
    }

    /**
     * Get the request content.
     *
     * @return the content to be sent
     */
    public Flux<ByteBuffer> getBody() {
        return body == null ? null : body.toFluxByteBuffer();
    }

    /**
     * Get the request content.
     *
     * @return the content to be sent
     */
    public BinaryData getBodyAsBinaryData() {
        return body;
    }

    /**
     * Set the request content.
     * <p>
     * The Content-Length header will be set based on the given content's length.
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(String content) {
        return setBody(BinaryData.fromString(content));
    }

    /**
     * Set the request content.
     * <p>
     * The Content-Length header will be set based on the given content's length.
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(byte[] content) {
        return setBody(BinaryData.fromBytes(content));
    }

    /**
     * Set request content.
     * <p>
     * Caller must set the Content-Length header to indicate the length of the content, or use Transfer-Encoding:
     * chunked.
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(Flux<ByteBuffer> content) {
        if (content != null) {
            this.body = BinaryDataHelper.createBinaryData(new FluxByteBufferContent(content));
        } else  {
            this.body = null;
        }
        return this;
    }

    /**
     * Set request content.
     * <p>
     * If provided content has known length, i.e. {@link BinaryData#getLength()} returns non-null then
     * Content-Length header is updated. Otherwise,
     * if provided content has unknown length, i.e. {@link BinaryData#getLength()} returns null then
     * the caller must set the Content-Length header to indicate the length of the content, or use Transfer-Encoding:
     * chunked.
     *
     * @param content the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(BinaryData content) {
        this.body = content;
        if (content != null && content.getLength() != null) {
            setContentLength(content.getLength());
        }
        return this;
    }

    private void setContentLength(long contentLength) {
        headers.set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
    }

    /**
     * Creates a copy of the request.
     * <p>
     * The main purpose of this is so that this HttpRequest can be changed and the resulting HttpRequest can be a
     * backup. This means that the cloned HttpHeaders and body must not be able to change from side effects of this
     * HttpRequest.
     *
     * @return a new HTTP request instance with cloned instances of all mutable properties.
     */
    public HttpRequest copy() {
        final HttpHeaders bufferedHeaders = new HttpHeaders(headers);
        return new HttpRequest(httpMethod, url, bufferedHeaders, body);
    }
}
