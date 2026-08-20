// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.ServerSentEvent;
import com.azure.core.http.ServerSentEventListener;
import com.azure.core.http.ServerSentEventStreams;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.BinaryData;
import org.reactivestreams.Subscription;
import org.junit.jupiter.api.Test;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerSentEventStreamTests {
    @Test
    public void toFluxParsesFragmentedEventMetadata() {
        byte[] bytes = ("\uFEFF: comment\rid: 42\r\nevent: greeting\nretry: 2000\ndata: caf\u00e9\r\ndata: second\n\n")
            .getBytes(StandardCharsets.UTF_8);
        List<ByteBuffer> buffers = new ArrayList<>();
        for (byte value : bytes) {
            buffers.add(ByteBuffer.wrap(new byte[] { value }));
        }

        TestResponse response = response(200, BinaryData.fromFlux(Flux.fromIterable(buffers), null, false).block());

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data)).assertNext(event -> {
            assertEquals("42", event.getId());
            assertEquals("greeting", event.getEvent());
            assertEquals("caf\u00e9\nsecond", event.getData());
            assertEquals("comment", event.getComment());
            assertEquals(Duration.ofSeconds(2), event.getRetryAfter());
        }).verifyComplete();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxDeserializesMultipleBufferedEventsOnDemand() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\ndata: two\n\ndata: three\n\n"));
        AtomicInteger conversionCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> {
            conversionCount.incrementAndGet();
            return data;
        }), 0)
            .thenRequest(1)
            .assertNext(event -> assertEquals("one", event.getData()))
            .then(() -> assertEquals(1, conversionCount.get()))
            .thenRequest(1)
            .assertNext(event -> assertEquals("two", event.getData()))
            .then(() -> assertEquals(2, conversionCount.get()))
            .thenRequest(1)
            .assertNext(event -> assertEquals("three", event.getData()))
            .then(() -> assertEquals(3, conversionCount.get()))
            .verifyComplete();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxNullConversionDoesNotConsumeDemand() {
        TestResponse response = response(200, BinaryData.fromString("data: skip\n\ndata: deliver\n\n"));
        AtomicInteger conversionCount = new AtomicInteger();

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> {
            conversionCount.incrementAndGet();
            return "skip".equals(data) ? null : data;
        }), 0)
            .thenRequest(1)
            .assertNext(event -> assertEquals("deliver", event.getData()))
            .then(() -> assertEquals(2, conversionCount.get()))
            .verifyComplete();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxCompletesOnEofWithoutReconnecting() {
        TestResponse response = response(200, BinaryData.fromString("id: 1\nretry: 0\ndata: one\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .assertNext(event -> assertEquals("one", event.getData()))
            .verifyComplete();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxReturnsEmptyForNoContent() {
        TestResponse response = response(204, null);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data)).verifyComplete();
    }

    @Test
    public void toFluxClosesBodyOwnedNoContentResponse() {
        AtomicBoolean bodyClosed = new AtomicBoolean();
        BinaryData body
            = BinaryData
                .fromFlux(Flux.using(() -> bodyClosed, ignored -> Flux.never(), ignored -> bodyClosed.set(true)), null,
                    false)
                .block();
        ResponseBase<Object, BinaryData> response = new ResponseBase<>(null, 204, new HttpHeaders(), body, null);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data)).verifyComplete();

        assertTrue(bodyClosed.get());
    }

    @Test
    public void toFluxRejectsInvalidContentTypeAndClosesResponse() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\n"), "application/json");

        assertThrows(IllegalStateException.class,
            () -> ServerSentEventStreams.toFlux(response, (event, data) -> data).blockLast());

        assertTrue(response.closed.get());
    }

    @Test
    public void responseDecodersIgnoreDeclaredCharset() {
        byte[] bytes = "data: caf\u00e9\n\n".getBytes(StandardCharsets.UTF_8);
        List<ByteBuffer> buffers = new ArrayList<>();
        for (byte value : bytes) {
            buffers.add(ByteBuffer.wrap(new byte[] { value }));
        }
        TestResponse response = response(200, BinaryData.fromFlux(Flux.fromIterable(buffers), null, false).block(),
            "text/event-stream; charset=UTF-16BE");

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .assertNext(event -> assertEquals("caf\u00e9", event.getData()))
            .verifyComplete();

        assertTrue(response.closed.get());

        TestResponse syncResponse = response(200, BinaryData.fromBytes(bytes), "text/event-stream; charset=UTF-16BE");
        List<String> events = new ArrayList<>();

        ServerSentEventStreams.listen(syncResponse, (event, data) -> data, event -> events.add(event.getData()));

        assertEquals(1, events.size());
        assertEquals("caf\u00e9", events.get(0));
        assertTrue(syncResponse.closed.get());
    }

    @Test
    public void toFluxCompletesForEmptyBody() {
        TestResponse response = response(200, BinaryData.fromBytes(new byte[0]));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (eventName, data) -> data)).verifyComplete();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxCompletesForBomOnlyBody() {
        TestResponse response
            = response(200, BinaryData.fromBytes(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (eventName, data) -> data)).verifyComplete();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxRejectsNonUtf8Bom() {
        TestResponse response = response(200, BinaryData.fromBytes(new byte[] { (byte) 0xFF, (byte) 0xFE }));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (eventName, data) -> data))
            .expectError(IllegalStateException.class)
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxFailsForTruncatedBomPrefixWithoutNullPointerException() {
        TestResponse response = response(200, BinaryData.fromBytes(new byte[] { (byte) 0xEF }));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (eventName, data) -> data))
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && !(error instanceof NullPointerException))
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxIgnoresUnrecognizedCharset() {
        TestResponse response
            = response(200, BinaryData.fromString("data: caf\u00e9\n\n"), "text/event-stream; charset=not-a-charset");

        ServerSentEvent<String> event = ServerSentEventStreams.toFlux(response, (eventName, data) -> data).blockLast();

        assertEquals("caf\u00e9", event.getData());
        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxConsumesBodyOwnedNonCloseableResponse() {
        AtomicBoolean bodyClosed = new AtomicBoolean();
        BinaryData body = BinaryData.fromFlux(Flux.using(() -> bodyClosed,
            ignored -> Flux.just(ByteBuffer.wrap("data: one\n\n".getBytes(StandardCharsets.UTF_8))),
            ignored -> bodyClosed.set(true)), null, false).block();
        ResponseBase<Object, BinaryData> response = new ResponseBase<>(null, 200,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream"), body, null);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .assertNext(event -> assertEquals("one", event.getData()))
            .verifyComplete();

        assertTrue(bodyClosed.get());
    }

    @Test
    public void toFluxClosesBodyOwnedNonCloseableResponseOnValidationFailure() {
        AtomicBoolean bodyClosed = new AtomicBoolean();
        BinaryData body
            = BinaryData
                .fromFlux(Flux.using(() -> bodyClosed, ignored -> Flux.never(), ignored -> bodyClosed.set(true)), null,
                    false)
                .block();
        ResponseBase<Object, BinaryData> response = new ResponseBase<>(null, 200, new HttpHeaders(), body, null);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .expectErrorMessage(
                "Expected a successful server-sent event response to have Content-Type " + "'text/event-stream'.")
            .verify();

        assertTrue(bodyClosed.get());
    }

    @Test
    public void toFluxRejectsMissingContentTypeAndClosesResponse() {
        TestResponse response = new TestResponse(200, new HttpHeaders(), BinaryData.fromString("data: one\n\n"));

        assertThrows(IllegalStateException.class,
            () -> ServerSentEventStreams.toFlux(response, (event, data) -> data).blockLast());

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxRejectsNullBody() {
        TestResponse response = response(200, null);

        assertThrows(NullPointerException.class,
            () -> ServerSentEventStreams.toFlux(response, (event, data) -> data).blockLast());

    }

    @Test
    public void toFluxRejectsUnsupportedStatusAndClosesResponse() {
        TestResponse response = response(201, BinaryData.fromString("data: one\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .expectErrorMessage("Expected a server-sent event response to have status code 200 or 204.")
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxDoesNotClaimOrCloseResponseBeforeSubscription() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\n"));

        ServerSentEventStreams.toFlux(response, (event, data) -> data);

        assertFalse(response.closed.get());
    }

    @Test
    public void toFluxAllowsOnlyOneSubscription() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\n"));
        Flux<ServerSentEvent<String>> events = ServerSentEventStreams.toFlux(response, (event, data) -> data);

        StepVerifier.create(events).expectNextCount(1).verifyComplete();
        StepVerifier.create(events)
            .expectErrorMessage("This server-sent event stream supports only one subscription.")
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxCancellationClosesResponse() {
        AtomicBoolean cancelled = new AtomicBoolean();
        BinaryData body = BinaryData.fromFlux(
            Flux.concat(Flux.just(ByteBuffer.wrap("data: one\n\n".getBytes(StandardCharsets.UTF_8))), Flux.never())
                .doOnCancel(() -> cancelled.set(true)),
            null, false).block();
        TestResponse response = response(200, body);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .assertNext(event -> assertEquals("one", event.getData()))
            .thenCancel()
            .verify();

        assertTrue(cancelled.get());
        assertTrue(response.closed.get());
    }

    @Test
    public void decodeDoesNotCompleteAfterCancellationFromOnNext() {
        AtomicBoolean completed = new AtomicBoolean();
        AtomicReference<Throwable> error = new AtomicReference<>();
        List<String> events = new ArrayList<>();

        ServerSentEventStream.decode(BinaryData.fromString("data: one\n\n"), (event, data) -> data)
            .subscribe(new CoreSubscriber<ServerSentEvent<String>>() {
                private Subscription subscription;

                @Override
                public void onSubscribe(Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(1);
                }

                @Override
                public void onNext(ServerSentEvent<String> event) {
                    events.add(event.getData());
                    subscription.cancel();
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                }

                @Override
                public void onComplete() {
                    completed.set(true);
                }

                @Override
                public Context currentContext() {
                    return Context.empty();
                }
            });

        assertEquals(1, events.size());
        assertFalse(completed.get());
        assertNull(error.get());
    }

    @Test
    public void listenCompletesOnEofAndNotifiesLifecycleOnce() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\ndata: two\n\n"));
        List<String> events = new ArrayList<>();
        AtomicBoolean closed = new AtomicBoolean();

        ServerSentEventStreams.listen(response, (event, data) -> data, new ServerSentEventListener<String>() {
            @Override
            public void onEvent(ServerSentEvent<String> event) {
                events.add(event.getData());
            }

            @Override
            public void onClose() {
                assertFalse(closed.getAndSet(true));
            }
        });

        assertEquals(2, events.size());
        assertTrue(closed.get());
        assertTrue(response.closed.get());
    }

    @Test
    public void listenNotifiesErrorAndClosesResponse() {
        RuntimeException failure = new IllegalStateException("listener failed");
        AtomicReference<Throwable> reportedError = new AtomicReference<>();
        TestResponse response = response(200, BinaryData.fromString("data: one\n\n"));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ServerSentEventStreams.listen(response, (event, data) -> data, new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                    throw failure;
                }

                @Override
                public void onError(Throwable error) {
                    reportedError.set(error);
                }
            }));

        assertSame(failure, exception);
        assertSame(failure, reportedError.get());
        assertTrue(response.closed.get());
    }

    @Test
    public void listenStopsDeliveringBufferedEventsAfterInterruption() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\ndata: two\n\n"));
        List<String> events = new ArrayList<>();
        AtomicReference<Throwable> reportedError = new AtomicReference<>();

        try {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> ServerSentEventStreams
                .listen(response, (event, data) -> data, new ServerSentEventListener<String>() {
                    @Override
                    public void onEvent(ServerSentEvent<String> event) {
                        events.add(event.getData());
                        Thread.currentThread().interrupt();
                    }

                    @Override
                    public void onError(Throwable error) {
                        reportedError.set(error);
                    }
                }));

            assertTrue(Thread.currentThread().isInterrupted());
            assertSame(exception, reportedError.get());
            assertEquals(1, events.size());
            assertTrue(response.closed.get());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void idAndRetryAreMetadataOnly() {
        TestResponse response = response(200, BinaryData.fromString("id: 42\nretry: 1000\ndata: one\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data)).assertNext(event -> {
            assertEquals("42", event.getId());
            assertEquals(Duration.ofSeconds(1), event.getRetryAfter());
        }).verifyComplete();
    }

    @Test
    public void parserSkipsMetadataOnlyBlocksAndPersistsMetadata() {
        TestResponse response
            = response(200, BinaryData.fromString("id: 42\nretry: 1000\n\ndata: one\n\ndata: two\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data).collectList())
            .assertNext(events -> {
                assertEquals(2, events.size());
                for (ServerSentEvent<String> event : events) {
                    assertEquals("42", event.getId());
                    assertEquals(Duration.ofSeconds(1), event.getRetryAfter());
                }
            })
            .verifyComplete();
    }

    @Test
    public void parserResetsIdAndUsesDefaultEvent() {
        TestResponse response = response(200, BinaryData.fromString("id: 42\ndata: one\n\nid:\nevent:\ndata: two\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .assertNext(event -> assertEquals("42", event.getId()))
            .assertNext(event -> {
                assertEquals("", event.getId());
                assertEquals("message", event.getEvent());
            })
            .verifyComplete();
    }

    @Test
    public void parserDiscardsUnterminatedEventAtEof() {
        TestResponse response = response(200, BinaryData.fromString("event: partial\ndata: payload"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data)).verifyComplete();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxPropagatesBodyFailureAndClosesResponse() {
        IOException failure = new IOException("connection closed");
        BinaryData body = BinaryData
            .fromFlux(Flux.concat(Flux.just(ByteBuffer.wrap("data: one\n\n".getBytes(StandardCharsets.UTF_8))),
                Flux.error(failure)), null, false)
            .block();
        TestResponse response = response(200, body);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .assertNext(event -> assertEquals("one", event.getData()))
            .expectErrorMatches(error -> error == failure)
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxEmitsRequestedEventBeforeSynchronousBodyFailure() {
        IOException failure = new IOException("connection closed");
        Flux<ByteBuffer> source = Flux.from(subscriber -> subscriber.onSubscribe(new Subscription() {
            private boolean signalled;

            @Override
            public void request(long count) {
                if (!signalled) {
                    signalled = true;
                    subscriber.onNext(ByteBuffer.wrap("data: one\n\n".getBytes(StandardCharsets.UTF_8)));
                    subscriber.onError(failure);
                }
            }

            @Override
            public void cancel() {
            }
        }));
        TestResponse response = response(200, BinaryData.fromFlux(source, null, false).block());

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
            .assertNext(event -> assertEquals("one", event.getData()))
            .expectErrorMatches(error -> error == failure)
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void decodeHonorsReentrantDemandBeforeSynchronousBodyFailure() {
        IOException failure = new IOException("connection closed");
        Flux<ByteBuffer> source = Flux.from(subscriber -> subscriber.onSubscribe(new Subscription() {
            private boolean signalled;

            @Override
            public void request(long count) {
                if (!signalled) {
                    signalled = true;
                    subscriber.onNext(ByteBuffer.wrap("data: one\n\ndata: two\n\n".getBytes(StandardCharsets.UTF_8)));
                    subscriber.onError(failure);
                }
            }

            @Override
            public void cancel() {
            }
        }));
        List<String> events = new ArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        ServerSentEventStream.decode(BinaryData.fromFlux(source, null, false).block(), (event, data) -> data)
            .subscribe(new CoreSubscriber<ServerSentEvent<String>>() {
                private Subscription subscription;

                @Override
                public void onSubscribe(Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(1);
                }

                @Override
                public void onNext(ServerSentEvent<String> event) {
                    events.add(event.getData());
                    if (events.size() == 1) {
                        subscription.request(1);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                }

                @Override
                public void onComplete() {
                }

                @Override
                public Context currentContext() {
                    return Context.empty();
                }
            });

        assertEquals(2, events.size());
        assertEquals("one", events.get(0));
        assertEquals("two", events.get(1));
        assertSame(failure, error.get());
    }

    @Test
    public void toFluxPropagatesBodyFailurePublishedFromAnotherThread() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            for (int i = 0; i < 100; i++) {
                IOException failure = new IOException("connection closed " + i);
                Flux<ByteBuffer> source = Flux.from(subscriber -> subscriber.onSubscribe(new Subscription() {
                    private final AtomicBoolean signalled = new AtomicBoolean();

                    @Override
                    public void request(long count) {
                        if (signalled.compareAndSet(false, true)) {
                            executor.execute(() -> subscriber.onError(failure));
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                }));
                TestResponse response = response(200, BinaryData.fromFlux(source, null, false).block());

                StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data))
                    .expectErrorMatches(error -> error == failure)
                    .verify();

                assertTrue(response.closed.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void toFluxPropagatesConverterFailureAndClosesResponse() {
        RuntimeException failure = new IllegalStateException("invalid event");
        TestResponse response = response(200, BinaryData.fromString("data: invalid\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> {
            throw failure;
        })).expectErrorMatches(error -> error == failure).verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxEmitsTerminalEventAndCancelsRemainingBody() {
        AtomicBoolean cancelled = new AtomicBoolean();
        AtomicReference<Integer> conversionCount = new AtomicReference<>(0);
        BinaryData body
            = BinaryData
                .fromFlux(Flux.concat(
                    Flux.just(ByteBuffer
                        .wrap("data: one\n\ndata: [DONE]\n\ndata: ignored\n\n".getBytes(StandardCharsets.UTF_8))),
                    Flux.never()).doOnCancel(() -> cancelled.set(true)), null, false)
                .block();
        TestResponse response = response(200, body);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> {
            conversionCount.set(conversionCount.get() + 1);
            return data;
        }, event -> "[DONE]".equals(event.getData())))
            .assertNext(event -> assertEquals("one", event.getData()))
            .assertNext(event -> assertEquals("[DONE]", event.getData()))
            .verifyComplete();

        assertEquals(2, conversionCount.get());
        assertTrue(cancelled.get());
        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxFailsOnEofBeforeTerminalEvent() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data, event -> false))
            .assertNext(event -> assertEquals("one", event.getData()))
            .expectErrorMessage("The server-sent event stream ended before a terminal event.")
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxFailsOnMetadataOnlyEofBeforeTerminalEvent() {
        TestResponse response = response(200, BinaryData.fromString("retry: 1000\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data, event -> false))
            .expectErrorMessage("The server-sent event stream ended before a terminal event.")
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxCancellationBeforeTerminalDoesNotCreateEofError() {
        AtomicBoolean cancelled = new AtomicBoolean();
        BinaryData body = BinaryData.fromFlux(
            Flux.concat(Flux.just(ByteBuffer.wrap("data: one\n\n".getBytes(StandardCharsets.UTF_8))), Flux.never())
                .doOnCancel(() -> cancelled.set(true)),
            null, false).block();
        TestResponse response = response(200, body);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data, event -> false))
            .assertNext(event -> assertEquals("one", event.getData()))
            .thenCancel()
            .verify();

        assertTrue(cancelled.get());
        assertTrue(response.closed.get());
    }

    @Test
    public void toFluxNoContentDoesNotInvokeTerminalPredicate() {
        AtomicBoolean predicateInvoked = new AtomicBoolean();
        TestResponse response = response(204, null);

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data, event -> {
            predicateInvoked.set(true);
            return false;
        })).expectErrorMessage("The server-sent event stream ended before a terminal event.").verify();

        assertFalse(predicateInvoked.get());
    }

    @Test
    public void toFluxPropagatesTerminalPredicateFailureAndClosesResponse() {
        RuntimeException failure = new IllegalStateException("predicate failed");
        TestResponse response = response(200, BinaryData.fromString("data: one\n\n"));

        StepVerifier.create(ServerSentEventStreams.toFlux(response, (event, data) -> data, event -> {
            throw failure;
        }))
            .assertNext(event -> assertEquals("one", event.getData()))
            .expectErrorMatches(error -> error == failure)
            .verify();

        assertTrue(response.closed.get());
    }

    @Test
    public void listenDeliversTerminalEventAndSkipsBufferedEventsAfterIt() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\ndata: [DONE]\n\ndata: ignored\n\n"));
        List<String> events = new ArrayList<>();
        AtomicReference<Integer> conversionCount = new AtomicReference<>(0);

        ServerSentEventStreams.listen(response, (event, data) -> {
            conversionCount.set(conversionCount.get() + 1);
            return data;
        }, event -> "[DONE]".equals(event.getData()), event -> events.add(event.getData()));

        assertEquals(2, events.size());
        assertEquals("[DONE]", events.get(1));
        assertEquals(2, conversionCount.get());
        assertTrue(response.closed.get());
    }

    @Test
    public void listenFailsOnEofBeforeTerminalEventAndNotifiesListener() {
        TestResponse response = response(200, BinaryData.fromString("data: one\n\n"));
        AtomicReference<Throwable> reportedError = new AtomicReference<>();
        List<String> events = new ArrayList<>();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> ServerSentEventStreams
            .listen(response, (event, data) -> data, event -> false, new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                    events.add(event.getData());
                }

                @Override
                public void onError(Throwable error) {
                    reportedError.set(error);
                }
            }));

        assertEquals(1, events.size());
        assertSame(exception, reportedError.get());
        assertTrue(response.closed.get());
    }

    @Test
    public void listenNoContentDoesNotInvokeTerminalPredicate() {
        AtomicBoolean predicateInvoked = new AtomicBoolean();
        AtomicReference<Throwable> reportedError = new AtomicReference<>();
        TestResponse response = response(204, null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ServerSentEventStreams.listen(response, (event, data) -> data, event -> {
                predicateInvoked.set(true);
                return false;
            }, new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                }

                @Override
                public void onError(Throwable error) {
                    reportedError.set(error);
                }
            }));

        assertFalse(predicateInvoked.get());
        assertSame(exception, reportedError.get());
    }

    @Test
    public void listenPropagatesTerminalPredicateFailureAndClosesResponse() {
        RuntimeException failure = new IllegalStateException("predicate failed");
        AtomicReference<Throwable> reportedError = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();
        TestResponse response = response(200, BinaryData.fromString("data: one\n\n"));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ServerSentEventStreams.listen(response, (event, data) -> data, event -> {
                throw failure;
            }, new ServerSentEventListener<String>() {
                @Override
                public void onEvent(ServerSentEvent<String> event) {
                }

                @Override
                public void onError(Throwable error) {
                    reportedError.set(error);
                }

                @Override
                public void onClose() {
                    closed.set(true);
                }
            }));

        assertSame(failure, exception);
        assertSame(failure, reportedError.get());
        assertTrue(closed.get());
        assertTrue(response.closed.get());
    }

    private static TestResponse response(int statusCode, BinaryData body) {
        return response(statusCode, body, "text/event-stream");
    }

    private static TestResponse response(int statusCode, BinaryData body, String contentType) {
        return new TestResponse(statusCode, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType), body);
    }

    private static final class TestResponse extends ResponseBase<Object, BinaryData> {
        private final AtomicBoolean closed;

        private TestResponse(int statusCode, HttpHeaders headers, BinaryData value) {
            this(statusCode, headers, value, new AtomicBoolean());
        }

        private TestResponse(int statusCode, HttpHeaders headers, BinaryData value, AtomicBoolean closed) {
            super(null, statusCode, headers, trackBody(value, closed), null);
            this.closed = closed;
        }

        private static BinaryData trackBody(BinaryData body, AtomicBoolean closed) {
            return body == null
                ? null
                : BinaryData.fromFlux(body.toFluxByteBuffer()
                    .doOnComplete(() -> closed.set(true))
                    .doOnError(ignored -> closed.set(true))
                    .doOnCancel(() -> closed.set(true)), null, false).block();
        }
    }
}
