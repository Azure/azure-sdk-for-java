// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
    private volatile boolean requestBodyCopied = false;

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
        return new HttpRequest(httpMethod, url, bufferedHeaders, body);
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @return The request.
     */
    public HttpRequest copyWithRetryableBody() {
        try {
            final HttpHeaders bufferedHeaders = new HttpHeaders(headers);
            BinaryData retryableBody = getRetryableBody();
            return new HttpRequest(httpMethod, url, bufferedHeaders, retryableBody);
        } finally {
            requestBodyCopied = true;
        }
    }

    private BinaryData getRetryableBody() {
        if (body == null) {
            return null;
        }

        BinaryDataContent content = BinaryDataHelper.getContent(body);

        if (content instanceof ByteArrayContent
            || content instanceof SerializableContent
            || content instanceof StringContent
            || content instanceof FileContent) {
            return body;
        } else if (content instanceof InputStreamContent) {
            InputStream inputStream = content.toStream();
            Long contentLength = getContentLength();
            if (contentLength != null && contentLength < Integer.MAX_VALUE) {
                if (inputStream.markSupported()) {
                    if (requestBodyCopied) {
                        try {
                            inputStream.reset();
                        } catch (IOException e) {
                            throw LOGGER.logExceptionAsError(
                                new UncheckedIOException("Unable to reset request body", e));
                        }
                    }
                    inputStream.mark(contentLength.intValue());
                    return body;
                } else {
                    // If stream is not seekable buffer body and set it in both derived and original request.
                    byte[] bufferedContent = content.toBytes();
                    BinaryData bufferedBinaryData = BinaryData.fromBytes(bufferedContent);
                    this.setBody(bufferedBinaryData);
                    return bufferedBinaryData;
                }
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException("Non retryable request body"));
            }
        } else {
            /*
             Clone the original request to ensure that each try starts with the original (unmutated) request. We cannot
             simply call httpRequest.buffer() because although the body will start emitting from the beginning of the
             stream, the buffers that were emitted will have already been consumed (their position set to their limit),
             so it is not a true reset. By adding the map function, we ensure that anything which consumes the
             ByteBuffers downstream will only actually consume a duplicate so the original is preserved. This only
             duplicates the ByteBuffer object, not the underlying data.
             */
            Flux<ByteBuffer> bufferedBody = body.toFluxByteBuffer().map(ByteBuffer::duplicate);
            return BinaryDataHelper.createBinaryData(new FluxByteBufferContent(bufferedBody, body.getLength()));
        }
    }

    private Long getContentLength() {
        Long contentLength = body.getLength();
        if (contentLength == null) {
            String contentLengthHeaderValue = headers.getValue("Content-Length");
            if (contentLengthHeaderValue != null) {
                contentLength = Long.valueOf(contentLengthHeaderValue);
            }
        }
        return contentLength;
    }
}
