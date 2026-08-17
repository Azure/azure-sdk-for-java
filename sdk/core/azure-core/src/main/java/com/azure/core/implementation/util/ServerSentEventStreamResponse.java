// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Owns a physical response used by a logical server-sent event stream.
 *
 * <p>The source response must implement {@link Closeable}; otherwise this class rejects it with
 * {@link IllegalArgumentException} because it cannot guarantee physical response cleanup.</p>
 */
final class ServerSentEventStreamResponse implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServerSentEventStreamResponse.class);

    private final int statusCode;
    private final BinaryData body;
    private final Charset charset;
    private final Closeable response;
    private final AtomicBoolean closed = new AtomicBoolean();

    ServerSentEventStreamResponse(int statusCode, BinaryData body, Charset charset, Closeable response) {
        this.statusCode = statusCode;
        this.body = body;
        this.charset = charset;
        this.response = response;
    }

    /**
     * Creates a stream response from a closeable REST response.
     *
     * @param response The REST response.
     * @return The stream response.
     * @throws IllegalArgumentException If {@code response} does not implement {@link Closeable}.
     */
    static ServerSentEventStreamResponse fromResponse(Response<BinaryData> response) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        if (!(response instanceof Closeable)) {
            throw new IllegalArgumentException("'response' must own a closeable streaming response.");
        }
        if (response.getStatusCode() != 200 && response.getStatusCode() != 204) {
            closeResponse(response);
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Expected a server-sent event response to have status code 200 or 204."));
        }
        String contentType = response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);
        Charset charset = response.getStatusCode() == 200 ? HttpUtils.getTextEventStreamCharset(contentType) : null;
        if (response.getStatusCode() == 200
            && (!HttpUtils.isTextEventStreamContentType(contentType) || charset == null)) {
            closeResponse(response);
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Expected a successful server-sent event response to have Content-Type 'text/event-stream'."));
        }

        BinaryData body = response.getValue();
        if (response.getStatusCode() == 200) {
            if (body == null) {
                closeResponse(response);
                throw new NullPointerException("'response.getValue()' cannot be null unless the status code is 204.");
            }
        }
        return new ServerSentEventStreamResponse(response.getStatusCode(), body, charset, (Closeable) response);
    }

    private static void closeResponse(Response<BinaryData> response) {
        if (!(response instanceof Closeable)) {
            return;
        }

        try {
            ((Closeable) response).close();
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
    }

    int getStatusCode() {
        return statusCode;
    }

    BinaryData getBody() {
        return body;
    }

    Charset getCharset() {
        return charset;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        try {
            response.close();
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
    }
}
