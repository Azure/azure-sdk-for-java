// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Owns a physical response used by a logical server-sent event stream.
 */
final class ServerSentEventStreamResponse implements AutoCloseable {
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
     * Creates a stream response from a REST response.
     *
     * @param response The REST response.
     * @return The stream response.
     */
    static ServerSentEventStreamResponse fromResponse(Response<BinaryData> response) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        if (response.getStatusCode() != 200 && response.getStatusCode() != 204) {
            closeResponse(response);
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Expected a server-sent event response to have status code 200 or 204."));
        }
        String contentType = response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);
        if (response.getStatusCode() == 200 && !HttpUtils.isTextEventStreamContentType(contentType)) {
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
        return new ServerSentEventStreamResponse(response.getStatusCode(), body,
            response instanceof Closeable ? (Closeable) response : null);
    }

    private static void closeResponse(Response<BinaryData> response) {
        if (response instanceof Closeable) {
            close((Closeable) response);
            return;
        }

        closeBody(response.getValue());
    }

    private static void closeBody(BinaryData body) {
        if (body != null) {
            body.toFluxByteBuffer().subscribe(new BaseSubscriber<ByteBuffer>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    cancel();
                }
            });
        }
    }

    private static void close(Closeable response) {
        try {
            response.close();
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

        if (response != null) {
            close(response);
        } else if (statusCode == 204 && body != null) {
            closeBody(body);
        }
    }
}
