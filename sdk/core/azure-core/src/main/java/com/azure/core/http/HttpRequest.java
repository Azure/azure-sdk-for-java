// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
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
    private BinaryData data;

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
     * @param body the request content
     */
    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, Flux<ByteBuffer> body) {
        this(httpMethod, url, headers, createFluxBinaryData(body));
    }

    /**
     * Creates a new HttpRequest instance.
     *
     * @param httpMethod The HTTP request method.
     * @param url The address where the request will be sent.
     * @param headers The HTTP headers in the request.
     * @param data The request body content.
     */
    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, BinaryData data) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = headers;
        this.data = data;
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
            this.url = new URL(url);
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
     */
    public HttpRequest setHeader(String name, String value) {
        headers.set(name, value);
        return this;
    }

    /**
     * Get the request content.
     *
     * @return the content to be sent
     */
    public Flux<ByteBuffer> getBody() {
        return (data == null) ? null : data.toFluxByteBuffer();
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
        data = BinaryData.fromString(content);
        setContentLength(data.getLength());

        return this;
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
        data = BinaryData.fromBytes(content);
        setContentLength(data.getLength());

        return this;
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
        this.data = createFluxBinaryData(content);
        return this;
    }

    /**
     * Gets the {@link BinaryData} that represents the body of the request.
     *
     * @return The {@link BinaryData} request body.
     */
    public BinaryData getContent() {
        return data;
    }

    /**
     * Sets the {@link BinaryData} that represents the body of the request.
     *
     * @param data The {@link BinaryData} request body.
     * @return this HttpRequest
     */
    public HttpRequest setContent(BinaryData data) {
        this.data = data;
        return this;
    }

    private static BinaryData createFluxBinaryData(Flux<ByteBuffer> content) {
        return (content == null) ? null : BinaryDataHelper.createBinaryData(new FluxByteBufferContent(content));
    }

    private void setContentLength(Long contentLength) {
        if (contentLength == null) {
            headers.remove("Content-Length");
        } else {
            headers.set("Content-Length", String.valueOf(contentLength));
        }
    }

    /**
     * Creates a copy of the request.
     *
     * The main purpose of this is so that this HttpRequest can be changed and the resulting HttpRequest can be a
     * backup. This means that the cloned HttpHeaders and body must not be able to change from side effects of this
     * HttpRequest.
     *
     * @return a new HTTP request instance with cloned instances of all mutable properties.
     */
    public HttpRequest copy() {
        final HttpHeaders bufferedHeaders = new HttpHeaders(headers);
        final BinaryData bufferedData = (data == null)
            ? null
            : BinaryDataHelper.createBinaryData(BinaryDataHelper.getContent(data).copy());
        return new HttpRequest(httpMethod, url, bufferedHeaders, bufferedData);
    }
}
