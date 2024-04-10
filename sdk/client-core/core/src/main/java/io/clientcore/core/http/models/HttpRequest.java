// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.annotation.Metadata;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static io.clientcore.core.annotation.TypeConditions.FLUENT;

/**
 * The outgoing {@link HttpRequest}. It provides ways to construct {@link HttpRequest} with {@link HttpMethod},
 * {@link URL}, {@link HttpHeader} and request body.
 */
@Metadata(conditions = FLUENT)
public class HttpRequest {
    // HttpRequest is a highly used, short-lived class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpRequest.class);

    static {
        HttpRequestAccessHelper.setAccessor(HttpRequest::setRetryCount);
    }

    private HttpMethod httpMethod;
    private URL url;
    private HttpHeaders headers;
    private BinaryData body;
    private ServerSentEventListener serverSentEventListener;
    private RequestOptions options;
    private int retryCount;

    /**
     * Create a new {@link HttpRequest} instance.
     *
     * @param httpMethod The request {@link HttpMethod}.
     * @param url The target address to send the request to as a {@link URL}.
     */
    public HttpRequest(HttpMethod httpMethod, URL url) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = new HttpHeaders();
        this.options = new RequestOptions();
    }

    /**
     * Create a new {@link HttpRequest} instance.
     *
     * @param httpMethod The request {@link HttpMethod}.
     * @param url The target address to send the request to.
     *
     * @throws IllegalArgumentException If {@code url} is {@code null} or it cannot be parsed into a valid {@link URL}.
     */
    public HttpRequest(HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;

        setUrl(url);

        this.headers = new HttpHeaders();
        this.options = new RequestOptions();
    }

    /**
     * Get the request {@link HttpMethod}.
     *
     * @return The request {@link HttpMethod}.
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Set the request {@link HttpMethod}.
     *
     * @param httpMethod The request {@link HttpMethod}.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;

        return this;
    }

    /**
     * Get the target address as a {@link URL}.
     *
     * @return The target address as a {@link URL}.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url The target address as a {@link URL}.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setUrl(URL url) {
        this.url = url;

        return this;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url The target address as a {@link URL}.
     *
     * @return The updated {@link HttpRequest}.
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
     * Get the request {@link HttpHeaders headers}.
     *
     * @return The {@link HttpHeaders headers} to be sent.
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Set the request {@link HttpHeaders headers}.
     *
     * @param headers The {@link HttpHeaders headers} to set.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setHeaders(HttpHeaders headers) {
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
     * Set the request content.
     *
     * <p>If the provided content has known length, i.e. {@link BinaryData#getLength()} returns non-null then the
     * {@code Content-Length} header is updated. Otherwise, if the provided content has unknown length, i.e.
     * {@link BinaryData#getLength()} returns {@code null} then the caller must set the {@code Content-Length} header
     * to indicate the length of the content, or use {@code Transfer-Encoding: chunked}.</p>
     *
     * @param content The request content.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setBody(BinaryData content) {
        this.body = content;

        // TODO (alzimmer): should the Content-Length header be removed if content is null?
        if (content != null && content.getLength() != null) {
            headers.set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(content.getLength()));
        }

        return this;
    }

    /**
     * Get the request {@link RequestOptions options}.
     *
     * @return The request {@link RequestOptions options}.
     */
    public RequestOptions getOptions() {
        return options;
    }

    /**
     * Set the request {@link RequestOptions options}.
     *
     * @param options The request {@link RequestOptions options}.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setOptions(RequestOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");

        this.options = options;

        return this;
    }

    /**
     * Creates a copy of this {@link HttpRequest}.
     *
     * <p>The main purpose of this is so that this {@link HttpRequest} can be changed and the resulting
     * {@link HttpRequest} can be a backup. This means that the cloned {@link HttpHeaders} and body must not be able to
     * change from side effects of this {@link HttpRequest}.</p>
     *
     * @return A new {@link HttpRequest} instance with cloned instances of all mutable properties.
     */
    public HttpRequest copy() {
        return new HttpRequest(httpMethod, url)
            .setHeaders(new HttpHeaders(headers))
            .setBody(body)
            .setOptions(options.copy());
    }

    /**
     * Get the specified event stream {@link ServerSentEventListener listener} for this request.
     *
     * @return The {@link ServerSentEventListener listener} for this request.
     */
    public ServerSentEventListener getServerSentEventListener() {
        return serverSentEventListener;
    }

    /**
     * Set an event stream {@link ServerSentEventListener listener} for this request.
     *
     * @param serverSentEventListener The {@link ServerSentEventListener listener} to set for this request.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setServerSentEventListener(ServerSentEventListener serverSentEventListener) {
        this.serverSentEventListener = serverSentEventListener;

        return this;
    }

    /**
     * Gets the number of times the request has been retried.
     *
     * @return The number of times the request has been retried.
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Sets the number of times the request has been retried.
     *
     * @param retryCount The number of times the request has been retried.
     *
     * @return The updated {@link HttpRequest} object.
     */
    private HttpRequest setRetryCount(int retryCount) {
        this.retryCount = retryCount;

        return this;
    }
}
