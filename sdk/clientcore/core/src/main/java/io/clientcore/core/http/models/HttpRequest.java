// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static io.clientcore.core.annotations.MetadataProperties.FLUENT;

/**
 * The outgoing HTTP request. This class provides ways to construct it with an {@link HttpMethod}, {@link URI},
 * {@link HttpHeader} and request body.
 */
@Metadata(properties = FLUENT)
public class HttpRequest {
    // HttpRequest is a highly used, short-lived class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpRequest.class);

    static {
        HttpRequestAccessHelper.setAccessor(new HttpRequestAccessHelper.HttpRequestAccessor() {
            @Override
            public int getTryCount(HttpRequest httpRequest) {
                return httpRequest.getTryCount();
            }

            @Override
            public HttpRequest setTryCount(HttpRequest httpRequest, int tryCount) {
                return httpRequest.setTryCount(tryCount);
            }
        });
    }

    private HttpMethod httpMethod;
    private URI uri;
    private HttpHeaders headers;
    private BinaryData body;
    private ServerSentEventListener serverSentEventListener;
    private RequestContext requestContext;
    private int tryCount;

    /**
     * Create a new {@link HttpRequest} instance.
     */
    public HttpRequest() {
        this.headers = new HttpHeaders();
        this.requestContext = RequestContext.none();
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
     * @return The updated {@link HttpRequest}.
     * @throws NullPointerException if {@code httpMethod} is {@code null}.
     */
    public HttpRequest setMethod(HttpMethod httpMethod) {
        this.httpMethod = Objects.requireNonNull(httpMethod, "'httpMethod' cannot be null");
        return this;
    }

    /**
     * Get the target address as a {@link URI}.
     *
     * @return The target address as a {@link URI}.
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param uri The target address as a {@link URI}.
     * @return The updated {@link HttpRequest}.
     * @throws NullPointerException if {@code uri} is null.
     */
    public HttpRequest setUri(URI uri) {
        this.uri = Objects.requireNonNull(uri, "'uri' cannot be null");
        return this;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param uri The target address as a {@link URI}.
     * @return The updated {@link HttpRequest}.
     * @throws NullPointerException if {@code uri} is {@code null}.
     * @throws IllegalArgumentException If {@code uri} cannot be parsed into a valid {@link URI}.
     */
    public HttpRequest setUri(String uri) {
        try {
            this.uri = new URI(Objects.requireNonNull(uri, "'uri' cannot be null"));
        } catch (URISyntaxException ex) {
            throw LOGGER.throwableAtError().log("'uri' must be a valid URI.", ex, IllegalArgumentException::new);
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
     * Get the request context. If no context was provided, {@link RequestContext#none()} is returned.
     *
     * @return The {@link RequestContext}.
     */
    public RequestContext getContext() {
        return requestContext;
    }

    /**
     * Set the request context.
     *
     * @param requestContext The {@link RequestContext}.
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setContext(RequestContext requestContext) {
        this.requestContext = requestContext == null ? RequestContext.none() : requestContext;
        return this;
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
     * @return The updated {@link HttpRequest}.
     */
    public HttpRequest setServerSentEventListener(ServerSentEventListener serverSentEventListener) {
        this.serverSentEventListener = serverSentEventListener;
        return this;
    }

    /**
     * Gets the number of times the request has been attempted. It's 0 during the first attempt
     * and increments after attempt is made.
     *
     * @return The number of times the request has been attempted.
     */
    private int getTryCount() {
        return tryCount;
    }

    /**
     * Sets the number of times the request has been attempted. It's 0 during the first attempt
     * and increments after attempt is made.
     *
     * @param tryCount The number of times the request has been attempted.
     * @return The updated {@link HttpRequest} object.
     */
    private HttpRequest setTryCount(int tryCount) {
        this.tryCount = tryCount;
        return this;
    }
}
