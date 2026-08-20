// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Validates the response used by a logical server-sent event stream.
 */
final class ServerSentEventStreamResponse {
    private static final ClientLogger LOGGER = new ClientLogger(ServerSentEventStreamResponse.class);

    private final int statusCode;
    private final BinaryData body;

    ServerSentEventStreamResponse(int statusCode, BinaryData body) {
        this.statusCode = statusCode;
        this.body = body;
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
        if (response.getStatusCode() == 204) {
            closeBody(body);
        }
        return new ServerSentEventStreamResponse(response.getStatusCode(), body);
    }

    private static void closeResponse(Response<BinaryData> response) {
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

    int getStatusCode() {
        return statusCode;
    }

    BinaryData getBody() {
        return body;
    }
}
