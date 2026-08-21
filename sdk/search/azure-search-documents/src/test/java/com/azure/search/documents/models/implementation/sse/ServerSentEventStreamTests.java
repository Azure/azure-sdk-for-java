// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models.implementation.sse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.models.ServerSentEvent;
import com.azure.search.documents.models.ServerSentEventListener;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerSentEventStreamTests {
    @Test
    public void parsesFragmentedUtf8AndMetadata() {
        byte[] bytes
            = ("\uFEFF: comment\rid: 42\r\nevent: greeting\nretry: 2000\r" + "data: caf\u00e9\ndata: second\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8);
        List<ByteBuffer> buffers = new ArrayList<>();
        for (byte value : bytes) {
            buffers.add(ByteBuffer.wrap(new byte[] { value }));
        }
        BinaryData body = BinaryData.fromFlux(Flux.fromIterable(buffers), null, false).block();

        StepVerifier.create(toFlux(response(200, body))).assertNext(event -> {
            assertEquals("42", event.getId());
            assertEquals("greeting", event.getEvent());
            assertEquals("caf\u00e9\nsecond", event.getData());
            assertEquals("comment", event.getComment());
            assertEquals(Duration.ofSeconds(2), event.getRetryAfter());
        }).verifyComplete();
    }

    @Test
    public void metadataPersistsAndCanBeReset() {
        String stream = "id: 42\nretry: 1000\n\ndata: one\n\nid:\nevent:\ndata: stop\n\n";

        StepVerifier.create(toFlux(response(200, BinaryData.fromString(stream)))).assertNext(event -> {
            assertEquals("42", event.getId());
            assertEquals(Duration.ofSeconds(1), event.getRetryAfter());
        }).assertNext(event -> {
            assertEquals("", event.getId());
            assertEquals("message", event.getEvent());
            assertEquals(Duration.ofSeconds(1), event.getRetryAfter());
        }).verifyComplete();
    }

    @Test
    public void supportsOnlyOneAsyncSubscription() {
        Flux<ServerSentEvent<String>> events = toFlux(response(200, BinaryData.fromString("data: stop\n\n")));

        StepVerifier.create(events).expectNextCount(1).verifyComplete();
        StepVerifier.create(events)
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && error.getMessage().contains("only one subscription"))
            .verify();
    }

    @Test
    public void normalEofCompletesWithoutReconnecting() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\ndata: two\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .expectNextCount(2)
            .verifyComplete();
        StepVerifier.create(ServerSentEventStreams.toFlux(response(204, null), (event, data) -> data)).verifyComplete();
    }

    @Test
    public void terminalEventIsInclusiveAndCancelsRemainingBody() {
        AtomicBoolean cancelled = new AtomicBoolean();
        BinaryData body
            = BinaryData.fromFlux(Flux.concat(Flux.just(buffer("data: stop\n\ndata: ignored\n\n")), Flux.never())
                .doOnCancel(() -> cancelled.set(true)), null, false).block();

        StepVerifier.create(toFlux(response(200, body)))
            .assertNext(event -> assertEquals("stop", event.getData()))
            .verifyComplete();

        assertTrue(cancelled.get());
    }

    @Test
    public void eofBeforeTerminalEventFails() {
        StepVerifier.create(toFlux(response(200, BinaryData.fromString("data: one\n\n"))))
            .expectNextCount(1)
            .expectErrorMatches(error -> error instanceof IllegalStateException
                && error.getMessage().contains("before a terminal event"))
            .verify();
    }

    @Test
    public void validatesStatusContentTypeAndBody() {
        assertThrows(IllegalStateException.class,
            () -> toFlux(response(201, BinaryData.fromString("data: stop\n\n"))).blockLast());
        assertThrows(IllegalStateException.class,
            () -> toFlux(response(200, BinaryData.fromString("data: stop\n\n"), "application/json")).blockLast());
        assertThrows(IllegalStateException.class,
            () -> toFlux(new TestResponse(200, new HttpHeaders(), BinaryData.fromString("data: stop\n\n")))
                .blockLast());
        assertThrows(IllegalStateException.class, () -> toFlux(response(200, BinaryData.fromString("data: stop\n\n"),
            "text/event-stream; charset=utf-8, application/json")).blockLast());
        assertThrows(NullPointerException.class, () -> toFlux(response(200, null)).blockLast());
    }

    @Test
    public void malformedUtf8FailsDecoding() {
        byte[] prefix = "data: ".getBytes(StandardCharsets.UTF_8);
        byte[] stream = Arrays.copyOf(prefix, prefix.length + 3);
        stream[prefix.length] = (byte) 0xC3;
        stream[prefix.length + 1] = '\n';
        stream[prefix.length + 2] = '\n';
        BinaryData body = BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(stream)), null, false).block();

        StepVerifier.create(toFlux(response(200, body)))
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && error.getMessage().contains("Failed to decode"))
            .verify();
    }

    @Test
    public void validationFailureCancelsBody() {
        AtomicBoolean cancelled = new AtomicBoolean();
        BinaryData body
            = BinaryData.fromFlux(Flux.<ByteBuffer>never().doOnCancel(() -> cancelled.set(true)), null, false).block();

        assertThrows(IllegalStateException.class, () -> toFlux(response(200, body, "application/json")).blockLast());
        assertTrue(cancelled.get());
    }

    @Test
    public void noContentFailsWithoutInvokingTerminalPredicate() {
        AtomicBoolean invoked = new AtomicBoolean();
        Flux<ServerSentEvent<String>> events
            = ServerSentEventStreams.toFlux(response(204, null), (event, data) -> data, event -> {
                invoked.set(true);
                return true;
            });

        StepVerifier.create(events).expectError(IllegalStateException.class).verify();
        assertFalse(invoked.get());
    }

    @Test
    public void converterFailurePropagatesAndCancelsBody() {
        RuntimeException failure = new IllegalArgumentException("bad data");
        AtomicBoolean cancelled = new AtomicBoolean();
        BinaryData body
            = BinaryData
                .fromFlux(
                    Flux.concat(Flux.just(buffer("data: one\n\n")), Flux.never()).doOnCancel(() -> cancelled.set(true)),
                    null, false)
                .block();

        StepVerifier.create(ServerSentEventStreams.toFlux(response(200, body), (event, data) -> {
            throw failure;
        }, event -> false)).expectErrorMatches(error -> error == failure).verify();

        assertTrue(cancelled.get());
    }

    @Test
    public void downstreamCancellationCancelsBodyWithoutEofFailure() {
        AtomicBoolean cancelled = new AtomicBoolean();
        BinaryData body
            = BinaryData
                .fromFlux(
                    Flux.concat(Flux.just(buffer("data: one\n\n")), Flux.never()).doOnCancel(() -> cancelled.set(true)),
                    null, false)
                .block();

        StepVerifier.create(toFlux(response(200, body))).expectNextCount(1).thenCancel().verify();

        assertTrue(cancelled.get());
    }

    @Test
    public void syncListenerReceivesTerminalBeforeClose() {
        List<String> events = new ArrayList<>();
        AtomicBoolean closed = new AtomicBoolean();

        ServerSentEventStreams.listen(
            response(200, BinaryData.fromString("data: one\n\ndata: stop\n\ndata: ignored\n\n")), (event, data) -> data,
            event -> "stop".equals(event.getData()), new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                    assertFalse(closed.get());
                    events.add(event.getData());
                }

                @Override
                public void onClose() {
                    assertFalse(closed.getAndSet(true));
                }
            });

        assertEquals(2, events.size());
        assertEquals("stop", events.get(1));
        assertTrue(closed.get());
    }

    @Test
    public void syncListenerReportsEofFailureAndClosesOnce() {
        AtomicReference<Throwable> reportedError = new AtomicReference<>();
        AtomicInteger closeCount = new AtomicInteger();

        IllegalStateException failure = assertThrows(IllegalStateException.class,
            () -> ServerSentEventStreams.listen(response(200, BinaryData.fromString("data: one\n\n")),
                (event, data) -> data, event -> false, new ServerSentEventListener<String>() {
                    @Override
                    public void onEvent(ServerSentEvent<String> event) {
                    }

                    @Override
                    public void onError(Throwable error) {
                        reportedError.set(error);
                    }

                    @Override
                    public void onClose() {
                        closeCount.incrementAndGet();
                    }
                }));

        assertSame(failure, reportedError.get());
        assertEquals(1, closeCount.get());
    }

    private static Flux<ServerSentEvent<String>> toFlux(TestResponse response) {
        return ServerSentEventStreams.toFlux(response, (event, data) -> data,
            event -> "stop".equals(event.getData()) || "greeting".equals(event.getEvent()));
    }

    private static ByteBuffer buffer(String value) {
        return ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
    }

    private static TestResponse response(int statusCode, BinaryData body) {
        return response(statusCode, body, "text/event-stream; charset=utf-8");
    }

    private static TestResponse response(int statusCode, BinaryData body, String contentType) {
        return new TestResponse(statusCode, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType), body);
    }

    private static final class TestResponse extends ResponseBase<Object, BinaryData> {
        private TestResponse(int statusCode, HttpHeaders headers, BinaryData value) {
            super(null, statusCode, headers, value, null);
        }
    }
}
