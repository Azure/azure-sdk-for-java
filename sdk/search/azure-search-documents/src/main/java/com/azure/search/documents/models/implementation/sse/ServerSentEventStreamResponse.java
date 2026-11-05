// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models.implementation.sse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

/**
 * Validates and exposes the fields used by a server-sent event stream.
 */
final class ServerSentEventStreamResponse {
    private final int statusCode;
    private final BinaryData body;

    private ServerSentEventStreamResponse(int statusCode, BinaryData body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    static ServerSentEventStreamResponse fromResponse(Response<BinaryData> response) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        int statusCode = response.getStatusCode();
        if (statusCode != 200 && statusCode != 204) {
            cancelBody(response.getValue());
            throw new IllegalStateException("Expected a server-sent event response to have status code 200 or 204.");
        }

        if (statusCode == 200 && !isTextEventStream(response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
            cancelBody(response.getValue());
            throw new IllegalStateException(
                "Expected a successful server-sent event response to have Content-Type 'text/event-stream'.");
        }

        BinaryData body = response.getValue();
        if (statusCode == 200 && body == null) {
            throw new NullPointerException("'response.getValue()' cannot be null unless the status code is 204.");
        }
        if (statusCode == 204) {
            cancelBody(body);
        }
        return new ServerSentEventStreamResponse(statusCode, body);
    }

    static void cancelBody(BinaryData body) {
        if (body == null) {
            return;
        }

        body.toFluxByteBuffer().subscribe(new BaseSubscriber<ByteBuffer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                cancel();
            }
        });
    }

    private static boolean isTextEventStream(String contentType) {
        if (contentType == null || contentType.indexOf(',') >= 0) {
            return false;
        }
        int parameterIndex = contentType.indexOf(';');
        String mediaType = parameterIndex < 0 ? contentType : contentType.substring(0, parameterIndex);
        return "text/event-stream".equals(mediaType.trim().toLowerCase(Locale.ROOT));
    }

    int getStatusCode() {
        return statusCode;
    }

    BinaryData getBody() {
        return body;
    }
}
