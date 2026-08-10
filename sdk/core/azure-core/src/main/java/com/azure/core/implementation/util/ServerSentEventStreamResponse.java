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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Owns a physical response used by a logical server-sent event stream.
 */
public final class ServerSentEventStreamResponse implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServerSentEventStreamResponse.class);

    private final int statusCode;
    private final BinaryData body;
    private final Closeable response;
    private final AtomicBoolean closed = new AtomicBoolean();

    ServerSentEventStreamResponse(int statusCode, BinaryData body, Closeable response) {
        this.statusCode = statusCode;
        this.body = body;
        this.response = response;
    }

    /**
     * Creates a stream response from a closeable REST response.
     *
     * @param response The REST response.
     * @return The stream response.
     */
    public static ServerSentEventStreamResponse fromResponse(Response<BinaryData> response) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        if (response.getStatusCode() != 204
            && !HttpUtils.isTextEventStreamContentType(response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
            closeResponse(response);
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Expected a successful server-sent event response to have Content-Type 'text/event-stream'."));
        }
        if (!(response instanceof Closeable)) {
            throw new IllegalArgumentException("'response' must own a closeable streaming response.");
        }

        BinaryData body = response.getValue();
        if (response.getStatusCode() != 204) {
            Objects.requireNonNull(body, "'response.getValue()' cannot be null unless the status code is 204.");
        }
        return new ServerSentEventStreamResponse(response.getStatusCode(), body, (Closeable) response);
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
