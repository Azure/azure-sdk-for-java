// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.annotation.Metadata;
import com.generic.core.http.Response;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Header;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import com.generic.core.util.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;

import static com.generic.core.annotation.TypeConditions.FLUENT;

/**
 * The outgoing {@link HttpRequest}. It provides ways to construct it with {@link HttpMethod}, {@link URL},
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
    private Function<Response<?>, ?> deserializationCallback;

    /**
     * Create a new {@link HttpRequest} instance.
     *
     * @param httpMethod The HTTP request {@link HttpMethod method}.
     * @param url The target address to send the {@link HttpRequest request} to.
     */
    public HttpRequest(HttpMethod httpMethod, URL url) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = new Headers();
        this.metadata = new HttpRequestMetadata();
    }

    /**
     * Create a new {@link HttpRequest} instance.
     *
     * @param httpMethod The HTTP request {@link HttpMethod method}.
     * @param url The target address to send the {@link HttpRequest request} to.
     *
     * @throws IllegalArgumentException If {@code url} is {@code null} or it cannot be parsed into a valid {@link URL}.
     */
    public HttpRequest(HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;

        setUrl(url);

        this.headers = new Headers();
        this.metadata = new HttpRequestMetadata();
    }

    /**
     * Get the {@link HttpRequest request's} {@link HttpMethod}.
     *
     * @return The {@link HttpRequest request's} {@link HttpMethod}.
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
     * Get the target address for the {@link HttpRequest request}.
     *
     * @return The target address.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Set the target address to send the {@link HttpRequest request} to.
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
     * Set the target address to send the {@link HttpRequest request} to.
     *
     * @param url Target address as {@link URL}.
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
     * Get the {@link HttpRequest request} {@link Headers}.
     *
     * @return The {@link Headers} to be sent.
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * Set the {@link HttpRequest request} {@link Headers}.
     *
     * @param headers The {@link Headers} to set.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setHeaders(Headers headers) {
        this.headers = headers;

        return this;
    }

    /**
     * Get the {@link HttpRequest request} content.
     *
     * @return The content to be sent.
     */
    public BinaryData getBody() {
        return body;
    }

    /**
     * Set the {@link HttpRequest request} content.
     *
     * <p>If the provided content has a known length (i.e. {@link BinaryData#getLength()} returns non-null), then the
     * {@code Content-Length} header is updated. Otherwise, if the provided content has an unknown length
     * (i.e. {@link BinaryData#getLength()} returns {@code null}), then the caller must set the {@code Content-Length}
     * header to indicate the length of the content, or use the {@code transfer-encoding: chunked} header.</p>
     *
     * @param content The {@link HttpRequest request} content.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setBody(BinaryData content) {
        this.body = content;

        // TODO (alzimmer): Should the Content-Length header be removed if the content is null?
        if (content != null && content.getLength() != null) {
            headers.set(HeaderName.CONTENT_LENGTH, String.valueOf(content.getLength()));
        }

        return this;
    }

    /**
     * Get the {@link HttpRequest request} {@link HttpRequestMetadata metadata}.
     *
     * @return The {@link HttpRequest request} {@link HttpRequestMetadata metadata}.
     */
    public HttpRequestMetadata getMetadata() {
        return metadata;
    }

    /**
     * Set the {@link HttpRequest request} {@link HttpRequestMetadata metadata}.
     *
     * @param metadata The {@link HttpRequest request} {@link HttpRequestMetadata metadata}.
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setMetadata(HttpRequestMetadata metadata) {
        this.metadata = metadata;

        return this;
    }

    /**
     * Creates a copy of the {@link HttpRequest request}.
     *
     * <p>The main purpose of this is so that this {@link HttpRequest} can be changed and the resulting
     * {@link HttpRequest} can be a backup. This means that the cloned {@link Headers} and body must not be able to
     * change from side effects of this {@link HttpRequest}.</p>
     *
     * @return A new {@link HttpRequest} instance with cloned instances of all mutable properties.
     */
    public HttpRequest copy() {
        return new HttpRequest(httpMethod, url)
            .setHeaders(new Headers(headers))
            .setBody(body)
            .setMetadata(metadata.copy());
    }

    /**
     * Get the specified event stream {@link ServerSentEventListener listener} for this {@link HttpRequest request}.
     *
     * @return The {@link ServerSentEventListener listener} for this {@link HttpRequest request}.
     */
    public ServerSentEventListener getServerSentEventListener() {
        return serverSentEventListener;
    }

    /**
     * Set an event stream {@link ServerSentEventListener listener} for this {@link HttpRequest request}.
     *
     * @param serverSentEventListener The {@link ServerSentEventListener listener} to set for this
     * {@link HttpRequest request}.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setServerSentEventListener(ServerSentEventListener serverSentEventListener) {
        this.serverSentEventListener = serverSentEventListener;

        return this;
    }

    /**
     * Get the {@link Function} that will handle deserialization for the body in the {@link Response} generated for
     * this {@link HttpRequest request}.
     *
     * @return The {@link Function} that will handle the {@link Response} body's deserialization.
     */
    public Function<Response<?>, ?> getResponseDeserializationCallback() {
        return deserializationCallback;
    }

    /**
     * Set a {@link Function} that will handle deserialization for the body in the {@link Response} generated for this
     * {@link HttpRequest request}.
     *
     * @param deserializationCallback The {@link Function} that will handle the {@link Response} body's deserialization.
     *
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setResponseDeserializationCallback(Function<Response<?>, ?> deserializationCallback) {
        this.deserializationCallback = deserializationCallback;

        return this;
    }
}
