// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.util.RequestContent;
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
    private final ClientLogger logger = new ClientLogger(HttpRequest.class);

    private HttpMethod httpMethod;
    private URL url;
    private HttpHeaders headers;
    private RequestContent requestContent;

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url the target address to send the request to
     */
    public HttpRequest(HttpMethod httpMethod, URL url) {
        this(httpMethod, url, new HttpHeaders(), (RequestContent) null);
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
        try {
            this.url = new URL(url);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL", ex));
        }
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
        this(httpMethod, url, headers, new FluxByteBufferContent(body));
    }

    /**
     * Creates a new {@link HttpRequest} instance.
     *
     * @param httpMethod The HTTP request method.
     * @param url The target address to send the request.
     * @param headers The HTTP headers of the request.
     * @param requestContent The {@link RequestContent}.
     */
    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, RequestContent requestContent) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = headers;
        this.requestContent = requestContent;
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
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL.", ex));
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
     * @return the content to be send
     */
    public Flux<ByteBuffer> getBody() {
        return (requestContent == null) ? null : requestContent.asFluxByteBuffer();
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
        return setRequestContent(RequestContent.fromString(content));
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
        setContentLength(content.length);
        return setBody(Flux.defer(() -> Flux.just(ByteBuffer.wrap(content))));
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
        this.requestContent = new FluxByteBufferContent(content);
        return this;
    }

    /**
     * Gets the HttpRequest's {@link RequestContent}.
     *
     * @return The {@link RequestContent}.
     */
    public RequestContent getRequestContent() {
        return this.requestContent;
    }

    /**
     * Sets the {@link RequestContent}.
     * <p>
     * If {@link RequestContent#getLength()} returns null for the passed {@link RequestContent} the caller must set the
     * Content-Length header to indicate the length of the content, or use Transfer-Encoding: chunked. Otherwise, {@link
     * RequestContent#getLength()} will be used to set the Content-Length header.
     *
     * @param requestContent The {@link RequestContent}.
     * @return The updated HttpRequest object.
     */
    public HttpRequest setRequestContent(RequestContent requestContent) {
        Long requestContentLength = requestContent.getLength();
        if (requestContentLength != null) {
            setContentLength(requestContentLength);
        }

        this.requestContent = requestContent;
        return this;
    }

    private void setContentLength(long contentLength) {
        headers.set("Content-Length", String.valueOf(contentLength));
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
        return new HttpRequest(httpMethod, url, bufferedHeaders, requestContent);
    }
}
