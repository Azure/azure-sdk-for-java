// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.annotation.Metadata;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.binarydata.BinaryData;

import java.net.MalformedURLException;
import java.net.URL;

import static com.generic.core.annotation.TypeConditions.FLUENT;

/**
 * The outgoing Http request. It provides ways to construct {@link HttpRequest} with {@link HttpMethod}, {@link URL},
 * {@link Header} and request body.
 */
@Metadata(conditions = FLUENT)
public class HttpRequest {
    // HttpRequest is a highly used, short-lived class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpRequest.class);

    private HttpMethod httpMethod;
    private URL url;
    private Headers headers;
    private BinaryData body;
    private HttpRequestMetadata metadata;
    private ServerSentEventListener serverSentEventListener;

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod The HTTP request method.
     * @param url The target address to send the request to.
     */
    public HttpRequest(HttpMethod httpMethod, URL url) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = new Headers();
        this.metadata = new HttpRequestMetadata();
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod The HTTP request method.
     * @param url The target address to send the request to.
     *
     * @throws IllegalArgumentException If {@code url} is null or it cannot be parsed into a valid URL.
     */
    public HttpRequest(HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;

        setUrl(url);

        this.headers = new Headers();
        this.metadata = new HttpRequestMetadata();
    }

    /**
     * Get the request method.
     *
     * @return The request method.
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Set the request method.
     *
     * @param httpMethod The request method.
     *
     * @return This HttpRequest.
     */
    public HttpRequest setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;

        return this;
    }

    /**
     * Get the target address.
     *
     * @return The target address.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url target address as {@link URL}.
     *
     * @return This HttpRequest.
     */
    public HttpRequest setUrl(URL url) {
        this.url = url;

        return this;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url Target address as {@link URL}.
     *
     * @return This HttpRequest.
     */
    @SuppressWarnings("deprecation")
    public HttpRequest setUrl(String url) {
        try {
            if (url != null) {
                this.url = new URL(url);
            }
        } catch (MalformedURLException ex) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'url' must be a valid URL.", ex));
        }

        return this;
    }

    /**
     * Get the request headers.
     *
     * @return The Headers to be sent.
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * Set the request headers.
     *
     * @param headers The set of headers.
     *
     * @return This HttpRequest.
     */
    public HttpRequest setHeaders(Headers headers) {
        this.headers = headers;

        return this;
    }

    /**
     * Get the request content.
     *
     * @return The content to be sent.
     */
    public BinaryData getBody() {
        return body;
    }

    /**
     * Set request content.
     * <p>
     * If provided content has known length, i.e. {@link BinaryData#getLength()} returns non-null then Content-Length
     * header is updated. Otherwise, if provided content has unknown length, i.e. {@link BinaryData#getLength()} returns
     * null then the caller must set the Content-Length header to indicate the length of the content, or use
     * Transfer-Encoding: chunked.
     *
     * @param content The request content.
     *
     * @return This HttpRequest.
     */
    public HttpRequest setBody(BinaryData content) {
        this.body = content;

        // TODO (alzimmer): should the Content-Length header be removed if content is null?
        if (content != null && content.getLength() != null) {
            headers.set(HeaderName.CONTENT_LENGTH, String.valueOf(content.getLength()));
        }

        return this;
    }

    /**
     * Get the request metadata.
     *
     * @return The request metadata.
     */
    public HttpRequestMetadata getMetadata() {
        return metadata;
    }

    /**
     * Set the request metadata.
     *
     * @param metadata The request metadata.
     * @return This HttpRequest.
     */
    public HttpRequest setMetadata(HttpRequestMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Creates a copy of the request.
     * <p>
     * The main purpose of this is so that this HttpRequest can be changed and the resulting HttpRequest can be a
     * backup. This means that the cloned Headers and body must not be able to change from side effects of this
     * HttpRequest.
     *
     * @return A new HTTP request instance with cloned instances of all mutable properties.
     */
    public HttpRequest copy() {
        return new HttpRequest(httpMethod, url)
            .setHeaders(new Headers(headers))
            .setBody(body)
            .setMetadata(metadata.copy());
    }

    /**
     * Get the specified event stream listener for this request.
     * @return the listener for this request.
     */
    public ServerSentEventListener getServerSentEventListener() {
        return serverSentEventListener;
    }

    /**
     * Set an event stream listener for this request.
     * @param serverSentEventListener the listener to set for this request.
     * @return This HttpRequest.
     */
    public HttpRequest setServerSentEventListener(ServerSentEventListener serverSentEventListener) {
        this.serverSentEventListener = serverSentEventListener;
        return this;
    }
}
