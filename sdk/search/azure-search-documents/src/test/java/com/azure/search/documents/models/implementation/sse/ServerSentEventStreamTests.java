// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models.implementation.sse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.models.ServerSentEvent;
import com.azure.search.documents.models.ServerSentEventListener;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerSentEventStreamTests {
    private static final String LIMIT_ERROR = "Server-sent event exceeded the maximum pending event size of 16 MiB.";

    @Test
    public void rejectsOversizedUnterminatedLine() {
        byte[] body = new byte[ServerSentEventStream.MAX_PENDING_EVENT_SIZE_BYTES + 1];
        Arrays.fill(body, (byte) 'x');

        StepVerifier.create(ServerSentEventStreams.toFlux(response(body), (event, data) -> data))
            .verifyErrorSatisfies(ServerSentEventStreamTests::assertLimitError);
    }

    @Test
    public void rejectsOversizedAggregateDataLines() {
        AtomicReference<Throwable> listenerError = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> ServerSentEventStreams
            .listen(response(aggregateDataLines()), (event, data) -> data, new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                }

                @Override
                public void onError(Throwable throwable) {
                    listenerError.set(throwable);
                }

                @Override
                public void onClose() {
                    closed.set(true);
                }
            }));

        assertLimitError(thrown);
        assertSame(thrown, listenerError.get());
        assertTrue(closed.get());
    }

    @Test
    public void acceptsEventExactlyAtLimit() {
        byte[] body = new byte[ServerSentEventStream.MAX_PENDING_EVENT_SIZE_BYTES + 2];
        byte[] prefix = "data:".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(prefix, 0, body, 0, prefix.length);
        Arrays.fill(body, prefix.length, ServerSentEventStream.MAX_PENDING_EVENT_SIZE_BYTES, (byte) 'x');
        body[ServerSentEventStream.MAX_PENDING_EVENT_SIZE_BYTES] = '\n';
        body[ServerSentEventStream.MAX_PENDING_EVENT_SIZE_BYTES + 1] = '\n';

        StepVerifier.create(ServerSentEventStreams.toFlux(response(body), (event, data) -> data))
            .assertNext(event -> assertEquals(ServerSentEventStream.MAX_PENDING_EVENT_SIZE_BYTES - prefix.length,
                event.getData().length()))
            .verifyComplete();
    }

    @Test
    public void resetsLimitBetweenEvents() {
        int lineLength = ServerSentEventStream.MAX_PENDING_EVENT_SIZE_BYTES / 2 + 1;
        byte[] body = new byte[(lineLength + 2) * 2];
        Arrays.fill(body, 0, lineLength, (byte) 'x');
        body[lineLength] = '\n';
        body[lineLength + 1] = '\n';
        Arrays.fill(body, lineLength + 2, body.length - 2, (byte) 'x');
        body[body.length - 2] = '\n';
        body[body.length - 1] = '\n';

        StepVerifier.create(ServerSentEventStreams.toFlux(response(body), (event, data) -> data)).verifyComplete();
    }

    private static byte[] aggregateDataLines() {
        int lineLength = 1024;
        int lineCount = ServerSentEventStream.MAX_PENDING_EVENT_SIZE_BYTES / lineLength + 1;
        byte[] body = new byte[lineCount * (lineLength + 1)];
        byte[] prefix = "data:".getBytes(StandardCharsets.UTF_8);
        for (int line = 0; line < lineCount; line++) {
            int offset = line * (lineLength + 1);
            System.arraycopy(prefix, 0, body, offset, prefix.length);
            Arrays.fill(body, offset + prefix.length, offset + lineLength, (byte) 'x');
            body[offset + lineLength] = '\n';
        }
        return body;
    }

    private static Response<BinaryData> response(byte[] body) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream");
        return new SimpleResponse<>(null, 200, headers, BinaryData.fromBytes(body));
    }

    private static void assertLimitError(Throwable throwable) {
        assertInstanceOf(IllegalStateException.class, throwable);
        assertEquals(LIMIT_ERROR, throwable.getMessage());
    }
}
