// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.ServerSentEvent;
import com.azure.core.http.ServerSentEventListener;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.UncheckedIOException;
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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerSentEventStreamTests {
    @Test
    public void syncReconnectsWithMetadataOnlyState() {
        BinaryData firstBody
            = BinaryData.fromString("id: first\nretry: 0\ndata: one\n\n" + "id: reconnect\nretry: invalid\n\n");
        List<ServerSentEvent<String>> events = new ArrayList<>();
        AtomicReference<String> reconnectEventId = new AtomicReference<>();
        AtomicInteger reconnectCount = new AtomicInteger();

        ServerSentEventStream.process(response(firstBody), eventId -> {
            reconnectEventId.set(eventId);
            reconnectCount.incrementAndGet();
            return response(BinaryData.fromString("data: [DONE]\n\n"));
        }, (event, data) -> data, event -> "[DONE]".equals(event.getData()), events::add);

        assertEquals(1, reconnectCount.get());
        assertEquals("reconnect", reconnectEventId.get());
        assertEquals(2, events.size());
        assertEquals("one", events.get(0).getData());
        assertEquals("reconnect", events.get(1).getId());
        assertEquals(Duration.ZERO, events.get(1).getRetryAfter());
    }

    @Test
    public void asyncReconnectsSeriallyWithRetainedState() {
        BinaryData firstBody = BinaryData.fromString("id: first\nretry: 0\ndata: one\n\nid: second\n\n");
        AtomicInteger reconnectCount = new AtomicInteger();
        AtomicReference<String> reconnectEventId = new AtomicReference<>();

        StepVerifier.create(ServerSentEventStream.decode(response(firstBody), eventId -> {
            reconnectEventId.set(eventId);
            reconnectCount.incrementAndGet();
            return Mono.just(response(BinaryData.fromString("data: [DONE]\n\n")));
        }, (event, data) -> data).takeUntil(event -> "[DONE]".equals(event.getData())))
            .assertNext(event -> assertEquals("one", event.getData()))
            .assertNext(event -> {
                assertEquals("[DONE]", event.getData());
                assertEquals("second", event.getId());
                assertEquals(Duration.ZERO, event.getRetryAfter());
            })
            .verifyComplete();

        assertEquals(1, reconnectCount.get());
        assertEquals("second", reconnectEventId.get());
    }

    @Test
    public void emptyIdOmitsLastEventIdOnReconnect() {
        BinaryData firstBody = BinaryData.fromString("id: first\nretry: 0\ndata: one\n\nid:\n\n");
        AtomicReference<String> reconnectEventId = new AtomicReference<>("not-called");

        ServerSentEventStream.process(response(firstBody), eventId -> {
            reconnectEventId.set(eventId);
            return response(BinaryData.fromString("data: [DONE]\n\n"));
        }, (event, data) -> data, event -> "[DONE]".equals(event.getData()), event -> {
        });

        assertNull(reconnectEventId.get());
    }

    @Test
    public void cleanCompletionWithoutRetryDoesNotReconnect() {
        AtomicInteger reconnectCount = new AtomicInteger();

        ServerSentEventStream.process(response(BinaryData.fromString("data: one\n\n")), eventId -> {
            reconnectCount.incrementAndGet();
            return response(BinaryData.fromString("data: unexpected\n\n"));
        }, (event, data) -> data, event -> false, event -> {
        });

        assertEquals(0, reconnectCount.get());
    }

    @Test
    public void asyncBodyErrorReconnectsWithRetainedRetry() {
        IOException disconnect = new IOException("connection closed");
        byte[] prefix = "retry: 0\ndata: one\n\n".getBytes(StandardCharsets.UTF_8);
        BinaryData body
            = BinaryData.fromFlux(Flux.concat(Flux.just(ByteBuffer.wrap(prefix)), Flux.error(disconnect)), null, false)
                .block();
        AtomicInteger reconnectCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStream.decode(response(body), eventId -> {
            reconnectCount.incrementAndGet();
            return Mono.just(response(BinaryData.fromString("data: [DONE]\n\n")));
        }, (event, data) -> data).takeUntil(event -> "[DONE]".equals(event.getData())))
            .assertNext(event -> assertEquals("one", event.getData()))
            .assertNext(event -> assertEquals("[DONE]", event.getData()))
            .verifyComplete();

        assertEquals(1, reconnectCount.get());
    }

    @Test
    public void syncBodyErrorReconnectsWithRetainedRetry() {
        IOException disconnect = new IOException("connection closed");
        byte[] prefix = "retry: 0\ndata: one\n\n".getBytes(StandardCharsets.UTF_8);
        BinaryData body
            = BinaryData.fromFlux(Flux.concat(Flux.just(ByteBuffer.wrap(prefix)), Flux.error(disconnect)), null, false)
                .block();
        AtomicInteger reconnectCount = new AtomicInteger();
        List<String> events = new ArrayList<>();

        ServerSentEventStream.process(response(body), eventId -> {
            reconnectCount.incrementAndGet();
            return response(BinaryData.fromString("data: [DONE]\n\n"));
        }, (event, data) -> data, event -> "[DONE]".equals(event.getData()), event -> events.add(event.getData()));

        assertEquals(1, reconnectCount.get());
        assertEquals(2, events.size());
        assertEquals("one", events.get(0));
        assertEquals("[DONE]", events.get(1));
    }

    @Test
    public void bodyErrorWithoutRetryTerminatesAsyncStream() {
        IOException disconnect = new IOException("connection closed");
        BinaryData body = BinaryData
            .fromFlux(Flux.concat(Flux.just(ByteBuffer.wrap("data: one\n\n".getBytes(StandardCharsets.UTF_8))),
                Flux.error(disconnect)), null, false)
            .block();
        AtomicInteger reconnectCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStream.decode(response(body), eventId -> {
            reconnectCount.incrementAndGet();
            return Mono.just(response(BinaryData.fromString("data: unexpected\n\n")));
        }, (event, data) -> data))
            .assertNext(event -> assertEquals("one", event.getData()))
            .expectErrorMatches(error -> error == disconnect)
            .verify();

        assertEquals(0, reconnectCount.get());
    }

    @Test
    public void deserializerErrorDoesNotReconnectAsyncStream() {
        RuntimeException deserializerError = new IllegalStateException("deserialization failed");
        BinaryData body = BinaryData.fromString("retry: 0\ndata: one\n\n");
        AtomicInteger reconnectCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStream.decode(response(body), eventId -> {
            reconnectCount.incrementAndGet();
            return Mono.just(response(BinaryData.fromString("data: unexpected\n\n")));
        }, (event, data) -> {
            throw deserializerError;
        })).expectErrorMatches(error -> error == deserializerError).verify();

        assertEquals(0, reconnectCount.get());
    }

    @Test
    public void asyncReconnectsWithoutGrowingPublisherDepth() {
        int eventCount = 10_000;
        BinaryData body = BinaryData.fromString("retry: 0\ndata: one\n\n");
        AtomicInteger reconnectCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStream.decode(response(body), eventId -> {
            reconnectCount.incrementAndGet();
            return Mono.just(response(BinaryData.fromString("data: one\n\n")));
        }, (event, data) -> data).take(eventCount)).expectNextCount(eventCount).verifyComplete();

        assertEquals(eventCount - 1, reconnectCount.get());
    }

    @Test
    public void zeroRetryUsesAsyncSchedulingBoundary() {
        BinaryData body = BinaryData.fromString("retry: 0\ndata: one\n\n");
        Thread subscriptionThread = Thread.currentThread();
        AtomicReference<Thread> reconnectThread = new AtomicReference<>();

        StepVerifier.create(ServerSentEventStream.decode(response(body), eventId -> {
            reconnectThread.set(Thread.currentThread());
            return Mono.just(response(BinaryData.fromString("data: [DONE]\n\n")));
        }, (event, data) -> data).takeUntil(event -> "[DONE]".equals(event.getData())))
            .expectNextCount(2)
            .verifyComplete();

        assertNotSame(subscriptionThread, reconnectThread.get());
    }

    @Test
    public void reconnectRequestErrorTerminatesAsyncStream() {
        RuntimeException requestError = new IllegalStateException("reconnect failed");
        BinaryData body = BinaryData.fromString("retry: 0\ndata: one\n\n");
        AtomicInteger reconnectCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStream.decode(response(body), eventId -> {
            reconnectCount.incrementAndGet();
            return Mono.error(requestError);
        }, (event, data) -> data))
            .assertNext(event -> assertEquals("one", event.getData()))
            .expectErrorMatches(error -> error == requestError)
            .verify();

        assertEquals(1, reconnectCount.get());
    }

    @Test
    public void listenerErrorDoesNotReconnect() {
        RuntimeException listenerError = new IllegalStateException("listener failed");
        BinaryData body = BinaryData.fromString("retry: 0\ndata: one\n\n");
        AtomicInteger reconnectCount = new AtomicInteger();
        AtomicReference<Throwable> reportedError = new AtomicReference<>();

        RuntimeException exception
            = assertThrows(RuntimeException.class, () -> ServerSentEventStream.process(response(body), eventId -> {
                reconnectCount.incrementAndGet();
                return response(BinaryData.fromString("data: unexpected\n\n"));
            }, (event, data) -> data, event -> false, new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                    throw listenerError;
                }

                @Override
                public void onError(Throwable error) {
                    reportedError.set(error);
                }
            }));

        assertSame(listenerError, exception);
        assertSame(listenerError, reportedError.get());
        assertEquals(0, reconnectCount.get());
    }

    @Test
    public void asyncCancellationDuringRetryDelayPreventsReconnect() {
        BinaryData body = BinaryData.fromString("retry: 60000\ndata: one\n\n");
        AtomicInteger reconnectCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStream.decode(response(body), eventId -> {
            reconnectCount.incrementAndGet();
            return Mono.just(response(BinaryData.fromString("data: unexpected\n\n")));
        }, (event, data) -> data)).assertNext(event -> assertEquals("one", event.getData())).thenCancel().verify();

        assertEquals(0, reconnectCount.get());
    }

    @Test
    public void maximumRetryValueDoesNotOverflowAsyncDelay() {
        BinaryData body = BinaryData.fromString("retry: 9223372036854775807\ndata: one\n\n");
        AtomicInteger reconnectCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStream.decode(response(body), eventId -> {
            reconnectCount.incrementAndGet();
            return Mono.just(response(BinaryData.fromString("data: unexpected\n\n")));
        }, (event, data) -> data))
            .assertNext(event -> assertEquals(Duration.ofMillis(Long.MAX_VALUE), event.getRetryAfter()))
            .thenCancel()
            .verify();

        assertEquals(0, reconnectCount.get());
    }

    @Test
    public void syncInterruptionDuringRetryDelayIsPropagated() {
        BinaryData body = BinaryData.fromString("retry: 60000\n\n");
        AtomicReference<Throwable> listenerError = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();
        ServerSentEventListener<String> listener = new ServerSentEventListener<String>() {
            @Override
            public void onEvent(ServerSentEvent<String> event) {
            }

            @Override
            public void onError(Throwable error) {
                listenerError.set(error);
            }

            @Override
            public void onClose() {
                closed.set(true);
            }
        };

        Thread.currentThread().interrupt();
        try {
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ServerSentEventStream.process(response(body),
                    eventId -> response(BinaryData.fromString("data: unexpected\n\n")), (event, data) -> data,
                    event -> false, listener));

            assertTrue(Thread.currentThread().isInterrupted());
            assertSame(exception, listenerError.get());
            assertTrue(closed.get());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void syncInterruptionBeforeZeroDelayReconnectIsPropagated() {
        BinaryData body = BinaryData.fromString("retry: 0\ndata: one\n\n");
        AtomicReference<Throwable> listenerError = new AtomicReference<>();
        AtomicInteger reconnectCount = new AtomicInteger();
        AtomicInteger eventCount = new AtomicInteger();
        ServerSentEventListener<String> listener = new ServerSentEventListener<String>() {
            @Override
            public void onEvent(ServerSentEvent<String> event) {
                eventCount.incrementAndGet();
                Thread.currentThread().interrupt();
            }

            @Override
            public void onError(Throwable error) {
                listenerError.set(error);
            }
        };

        try {
            RuntimeException exception
                = assertThrows(RuntimeException.class, () -> ServerSentEventStream.process(response(body), eventId -> {
                    reconnectCount.incrementAndGet();
                    return response(BinaryData.fromString("data: unexpected\n\n"));
                }, (event, data) -> data, event -> false, listener));

            assertTrue(Thread.currentThread().isInterrupted());
            assertSame(exception, listenerError.get());
            assertEquals(1, eventCount.get());
            assertEquals(0, reconnectCount.get());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void syncInterruptionAfterReconnectClosesAcquiredResponse() {
        BinaryData body = BinaryData.fromString("retry: 0\ndata: one\n\n");
        AtomicBoolean acquiredResponseClosed = new AtomicBoolean();
        AtomicReference<Throwable> listenerError = new AtomicReference<>();
        ServerSentEventListener<String> listener = new ServerSentEventListener<String>() {
            @Override
            public void onEvent(ServerSentEvent<String> event) {
            }

            @Override
            public void onError(Throwable error) {
                listenerError.set(error);
            }
        };

        try {
            RuntimeException exception
                = assertThrows(RuntimeException.class, () -> ServerSentEventStream.process(response(body), eventId -> {
                    Thread.currentThread().interrupt();
                    return new ServerSentEventStreamResponse(200, BinaryData.fromString("data: unexpected\n\n"),
                        () -> acquiredResponseClosed.set(true));
                }, (event, data) -> data, event -> false, listener));

            assertTrue(Thread.currentThread().isInterrupted());
            assertSame(exception, listenerError.get());
            assertTrue(acquiredResponseClosed.get());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void processParsesSupportedFields() {
        BinaryData body = BinaryData.fromString("\uFEFF: comment\nid: 42\nevent: stockUpdate\nretry: 2000\n"
            + "ignored: value\ndata: first\ndata: second\n\n");
        List<ServerSentEvent<String>> events = new ArrayList<>();

        ServerSentEventStream.process(body, (event, data) -> data, events::add);

        assertEquals(1, events.size());
        ServerSentEvent<String> event = events.get(0);
        assertEquals("42", event.getId());
        assertEquals("stockUpdate", event.getEvent());
        assertEquals("first\nsecond", event.getData());
        assertEquals("comment", event.getComment());
        assertEquals(Duration.ofSeconds(2), event.getRetryAfter());
    }

    @Test
    public void processSkipsBlocksWithoutDataAndUsesDefaultEvent() {
        BinaryData body
            = BinaryData.fromString(": keep alive\nretry: invalid\n\nid: contains\0null\nevent:\ndata: payload\n\n");
        AtomicReference<ServerSentEvent<String>> eventReference = new AtomicReference<>();

        ServerSentEventStream.process(body, (event, data) -> data, eventReference::set);

        ServerSentEvent<String> event = eventReference.get();
        assertNull(event.getId());
        assertEquals("message", event.getEvent());
        assertEquals("payload", event.getData());
        assertNull(event.getComment());
        assertNull(event.getRetryAfter());
    }

    @Test
    public void protocolMetadataPersistsAcrossEvents() {
        BinaryData body = BinaryData.fromString("id: 42\nretry: 2000\n\ndata: first\n\ndata: second\n\n");
        List<ServerSentEvent<String>> syncEvents = new ArrayList<>();

        ServerSentEventStream.process(body, (event, data) -> data, syncEvents::add);

        assertPersistentMetadata(syncEvents);
        StepVerifier.create(ServerSentEventStream.decode(body, (event, data) -> data).collectList())
            .assertNext(ServerSentEventStreamTests::assertPersistentMetadata)
            .verifyComplete();
    }

    @Test
    public void protocolEmptyIdResetsPersistentState() {
        BinaryData body = BinaryData.fromString("id: 42\ndata: first\n\nid:\ndata: second\n\n");

        StepVerifier.create(ServerSentEventStream.decode(body, (event, data) -> data))
            .assertNext(event -> assertEquals("42", event.getId()))
            .assertNext(event -> assertEquals("", event.getId()))
            .verifyComplete();
    }

    @Test
    public void protocolFinalMetadataOnlyBlockDoesNotEmitState() {
        BinaryData body = BinaryData.fromString("id: 1\nretry: 1000\ndata: first\n\nid: 2\nretry: 2000\n\n");
        List<ServerSentEvent<String>> syncEvents = new ArrayList<>();

        ServerSentEventStream.process(body, (event, data) -> data, syncEvents::add);

        assertEquals(1, syncEvents.size());
        assertInitialMetadata(syncEvents.get(0));
        StepVerifier.create(ServerSentEventStream.decode(body, (event, data) -> data))
            .assertNext(ServerSentEventStreamTests::assertInitialMetadata)
            .verifyComplete();
    }

    @Test
    public void processDeserializesTypedEventData() {
        BinaryData body = BinaryData.fromString("id: 42\nevent: number\ndata: 123\n\n");
        AtomicReference<ServerSentEvent<Integer>> eventReference = new AtomicReference<>();

        ServerSentEventStream.process(body, (eventName, data) -> {
            assertEquals("number", eventName);
            return Integer.parseInt(data);
        }, eventReference::set);

        ServerSentEvent<Integer> event = eventReference.get();
        assertEquals("42", event.getId());
        assertEquals("number", event.getEvent());
        assertEquals(123, event.getData());
    }

    @Test
    public void protocolDecodeParsesFragmentedUtf8AndLineEndings() {
        byte[] bytes = ("\uFEFF: comment\rid: 42\r\nevent: greeting\nretry: 2000\ndata: caf\u00e9\r\ndata: second\n\n")
            .getBytes(StandardCharsets.UTF_8);
        List<ByteBuffer> buffers = new ArrayList<>();
        for (byte value : bytes) {
            buffers.add(ByteBuffer.wrap(new byte[] { value }));
        }
        BinaryData body = BinaryData.fromFlux(Flux.fromIterable(buffers), null, false).block();

        StepVerifier.create(ServerSentEventStream.decode(body, (event, data) -> data)).assertNext(event -> {
            assertEquals("42", event.getId());
            assertEquals("greeting", event.getEvent());
            assertEquals("caf\u00e9\nsecond", event.getData());
            assertEquals("comment", event.getComment());
            assertEquals(Duration.ofSeconds(2), event.getRetryAfter());
        }).verifyComplete();
    }

    @Test
    public void protocolDecodeDiscardsFinalEventWithoutDelimiter() {
        BinaryData body
            = BinaryData
                .fromFlux(Flux.just(ByteBuffer.wrap("event: final\ndata: payload".getBytes(StandardCharsets.UTF_8))),
                    null, false)
                .block();

        StepVerifier.create(ServerSentEventStream.decode(body, (event, data) -> data)).verifyComplete();

        AtomicBoolean eventReceived = new AtomicBoolean();
        ServerSentEventStream.process(body, (event, data) -> data, event -> eventReceived.set(true));
        assertFalse(eventReceived.get());
    }

    @Test
    public void protocolDecodeDoesNotDispatchPartialEventAfterNetworkError() {
        IOException disconnect = new IOException("connection closed");
        Flux<ByteBuffer> content
            = Flux.concat(Flux.just(ByteBuffer.wrap("event: partial\ndata: pay".getBytes(StandardCharsets.UTF_8))),
                Flux.error(disconnect));
        BinaryData body = BinaryData.fromFlux(content, null, false).block();

        StepVerifier.create(ServerSentEventStream.decode(body, (event, data) -> data))
            .expectErrorMatches(error -> error == disconnect)
            .verify();
    }

    @Test
    public void processNotifiesAndRethrowsNetworkError() {
        IOException disconnect = new IOException("connection closed");
        Flux<ByteBuffer> content = Flux.concat(
            Flux.just(ByteBuffer.wrap("data: first\n\n".getBytes(StandardCharsets.UTF_8))), Flux.error(disconnect));
        BinaryData body = BinaryData.fromFlux(content, null, false).block();
        AtomicReference<Throwable> listenerError = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();

        UncheckedIOException exception = assertThrows(UncheckedIOException.class,
            () -> ServerSentEventStream.process(body, (event, data) -> data, new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                }

                @Override
                public void onError(Throwable error) {
                    listenerError.set(error);
                }

                @Override
                public void onClose() {
                    closed.set(true);
                }
            }));

        assertSame(disconnect, exception.getCause());
        assertSame(disconnect, listenerError.get());
        assertTrue(closed.get());
    }

    @Test
    public void processRethrowsDeserializerRuntimeException() {
        UncheckedIOException deserializationError = new UncheckedIOException(new IOException("invalid event"));
        BinaryData body = BinaryData.fromString("data: invalid\n\n");

        UncheckedIOException exception
            = assertThrows(UncheckedIOException.class, () -> ServerSentEventStream.process(body, (event, data) -> {
                throw deserializationError;
            }, ignored -> {
            }));

        assertSame(deserializationError, exception);
    }

    @Test
    public void processNotifiesAndRethrowsListenerRuntimeException() {
        RuntimeException listenerFailure = new IllegalStateException("listener failed");
        AtomicReference<Throwable> listenerError = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();
        BinaryData body = BinaryData.fromString("data: event\n\n");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ServerSentEventStream.process(body, (event, data) -> data, new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                    throw listenerFailure;
                }

                @Override
                public void onError(Throwable error) {
                    listenerError.set(error);
                }

                @Override
                public void onClose() {
                    closed.set(true);
                }
            }));

        assertSame(listenerFailure, exception);
        assertSame(listenerFailure, listenerError.get());
        assertTrue(closed.get());
    }

    @Test
    public void protocolDecodeCancellationCancelsFluxBackedBody() {
        AtomicBoolean cancelled = new AtomicBoolean();
        byte[] eventBytes = "data: payload\n\n".getBytes(StandardCharsets.UTF_8);
        Flux<ByteBuffer> content
            = Flux.concat(Flux.just(ByteBuffer.wrap(eventBytes)), Flux.never()).doOnCancel(() -> cancelled.set(true));
        BinaryData body = BinaryData.fromFlux(content, null, false).block();

        StepVerifier.create(ServerSentEventStream.decode(body, (event, data) -> data))
            .assertNext(event -> assertEquals("payload", event.getData()))
            .thenCancel()
            .verify();

        assertTrue(cancelled.get());
    }

    @Test
    public void protocolSyncAndAsyncDecodingHaveMatchingFraming() {
        String content = "event: first\ndata: one\r\n\r\nevent: second\ndata: two\n\n";
        List<ServerSentEvent<String>> syncEvents = new ArrayList<>();
        ServerSentEventStream.process(BinaryData.fromString(content), (event, data) -> data, syncEvents::add);

        List<ServerSentEvent<String>> asyncEvents
            = ServerSentEventStream.decode(BinaryData.fromString(content), (event, data) -> data).collectList().block();

        assertEquals(2, asyncEvents.size());
        assertEquals(Arrays.asList(syncEvents.get(0).getEvent(), syncEvents.get(1).getEvent()),
            Arrays.asList(asyncEvents.get(0).getEvent(), asyncEvents.get(1).getEvent()));
        assertEquals(Arrays.asList(syncEvents.get(0).getData(), syncEvents.get(1).getData()),
            Arrays.asList(asyncEvents.get(0).getData(), asyncEvents.get(1).getData()));
    }

    private static void assertPersistentMetadata(List<ServerSentEvent<String>> events) {
        assertEquals(2, events.size());
        assertEquals(Arrays.asList("first", "second"), Arrays.asList(events.get(0).getData(), events.get(1).getData()));
        for (ServerSentEvent<String> event : events) {
            assertEquals("42", event.getId());
            assertEquals(Duration.ofSeconds(2), event.getRetryAfter());
        }
    }

    private static void assertInitialMetadata(ServerSentEvent<String> event) {
        assertEquals("1", event.getId());
        assertEquals(Duration.ofSeconds(1), event.getRetryAfter());
    }

    private static ServerSentEventStreamResponse response(BinaryData body) {
        return new ServerSentEventStreamResponse(200, body, () -> {
        });
    }
}
