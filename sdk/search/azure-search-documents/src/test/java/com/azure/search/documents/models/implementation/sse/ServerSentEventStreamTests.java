// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models.implementation.sse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CloseableIterableStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerSentEventStreamTests {
    private static final BiFunction<String, String, String> CONVERTER = (event, data) -> event + ":" + data;

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 7, 8192 })
    public void syncFramingPreservesDataAndIgnoresMetadata(int chunkSize) throws IOException {
        TrackedInputStream body = new TrackedInputStream(framedEvents().getBytes(StandardCharsets.UTF_8), chunkSize);
        List<String> events = new ArrayList<>();

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER)) {
            stream.forEach(events::add);
            assertEquals(expectedFramedEvents(), events);
            assertEquals(1, body.closes.get());
        }

        assertEquals(1, body.closes.get());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 7, 8192 })
    public void asyncFramingPreservesDataAcrossBufferBoundaries(int chunkSize) {
        BinaryData body = reactiveBody(chunks(framedEvents().getBytes(StandardCharsets.UTF_8), chunkSize));

        StepVerifier.create(ServerSentEventStreams.toFlux(response(body), CONVERTER))
            .expectNextSequence(expectedFramedEvents())
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("borrowedBufferCases")
    public void asyncOwnsBorrowedBuffersBeforeDemandPausesAndPrefetch(boolean separateBuffer, boolean direct,
        boolean windowedReadOnly) {
        String firstEvents = "data: first\n\ndata: second\n\n";
        ByteBuffer firstStorage = windowedReadOnly ? windowedBuffer(firstEvents, direct) : buffer(firstEvents);
        ByteBuffer first = windowedReadOnly ? firstStorage.asReadOnlyBuffer() : firstStorage;
        ByteBuffer secondStorage
            = windowedReadOnly ? windowedBuffer("data: third\n\n", direct) : buffer("data: third\n\n");
        ByteBuffer second = windowedReadOnly ? secondStorage.asReadOnlyBuffer() : secondStorage;
        int start = windowedReadOnly ? 1 : 0;
        AtomicInteger returnedBuffers = new AtomicInteger();
        AtomicInteger conversions = new AtomicInteger();
        BinaryData body = reactiveBody(Flux.create(sink -> {
            assertTrue(sink.requestedFromDownstream() > 0);
            sink.next(first);
            returnedBuffers.incrementAndGet();
            if (separateBuffer) {
                assertTrue(sink.requestedFromDownstream() > 0,
                    "The next buffer must be prefetched, not source-queued.");
                sink.next(second);
                returnedBuffers.incrementAndGet();
                secondStorage.put(start + "data: ".length(), (byte) 0xFF);
            } else {
                firstStorage.put(start + firstEvents.indexOf("second"), (byte) 0xFF);
            }
            sink.complete();
        }));
        DemandSubscriber subscriber = new DemandSubscriber();

        ServerSentEventStreams.toFlux(response(body), (event, data) -> {
            conversions.incrementAndGet();
            return event + ":" + data;
        }).subscribe(subscriber);

        assertEquals(separateBuffer ? 2 : 1, returnedBuffers.get());
        assertEquals(Arrays.asList("message:first"), subscriber.events);
        assertEquals(1, conversions.get());
        assertEquals(start, first.position());
        assertEquals(start + firstEvents.length(), first.limit());
        assertEquals(start, second.position());
        assertEquals(start + "data: third\n\n".length(), second.limit());
        assertNull(subscriber.failure);
        assertFalse(subscriber.completed);

        subscriber.request(1);
        assertNull(subscriber.failure);
        assertEquals(Arrays.asList("message:first", "message:second"), subscriber.events);
        assertEquals(2, conversions.get());

        subscriber.request(2);
        assertNull(subscriber.failure);
        assertEquals(separateBuffer
            ? Arrays.asList("message:first", "message:second", "message:third")
            : Arrays.asList("message:first", "message:second"), subscriber.events);
        assertEquals(separateBuffer ? 3 : 2, conversions.get());
        assertTrue(subscriber.completed);
    }

    @Test
    public void syncIterationIsLazySingleUseAndHasNextDoesNotAdvance() throws IOException {
        TrackedInputStream body = new TrackedInputStream("data: first\n\ndata: second\n\n");
        AtomicInteger conversions = new AtomicInteger();

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), (event, data) -> {
                conversions.incrementAndGet();
                return data;
            })) {
            assertEquals(0, body.reads.get());
            Iterator<String> iterator = stream.iterator();
            assertEquals(0, body.reads.get());
            assertThrows(IllegalStateException.class, stream::iterator);

            assertTrue(iterator.hasNext());
            int reads = body.reads.get();
            assertTrue(iterator.hasNext());
            assertEquals(reads, body.reads.get());
            assertEquals(1, conversions.get());
            assertEquals("first", iterator.next());
            assertEquals(1, conversions.get());
            assertEquals("second", iterator.next());
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
            assertEquals(2, conversions.get());
            assertEquals(1, body.closes.get());
        }

        assertEquals(1, body.closes.get());
    }

    @Test
    public void syncCloseBeforeIterationReleasesBodyWithoutReading() throws IOException {
        TrackedInputStream body = new TrackedInputStream("data: unread\n\n");
        CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER);

        stream.close();
        stream.close();

        assertEquals(0, body.reads.get());
        assertEquals(1, body.closes.get());
        assertFalse(stream.iterator().hasNext());
    }

    @Test
    public void syncCloseBeforeIterationCancelsReactiveBodyWithoutRequesting() throws IOException {
        AtomicInteger subscriptions = new AtomicInteger();
        AtomicInteger requests = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        AtomicInteger buffers = new AtomicInteger();
        BinaryData body = reactiveBody(Flux.just(buffer("data: unread\n\n"))
            .concatWith(Flux.never())
            .doOnSubscribe(ignored -> subscriptions.incrementAndGet())
            .doOnRequest(ignored -> requests.incrementAndGet())
            .doOnNext(ignored -> buffers.incrementAndGet())
            .doOnCancel(cancellations::incrementAndGet));
        CloseableIterableStream<String> stream = ServerSentEventStreams.toIterableStream(response(body), CONVERTER);

        assertEquals(0, subscriptions.get());
        stream.close();
        stream.close();

        assertEquals(1, subscriptions.get());
        assertEquals(0, requests.get());
        assertEquals(0, buffers.get());
        assertEquals(1, cancellations.get());
    }

    @Test
    public void syncCloseAfterLookaheadDiscardsCachedEvent() throws IOException {
        TrackedInputStream body = new TrackedInputStream("data: first\n\ndata: second\n\n");
        CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER);
        Iterator<String> iterator = stream.iterator();
        assertTrue(iterator.hasNext());

        stream.close();
        stream.close();

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
        assertEquals(1, body.closes.get());
    }

    @Test
    public void javaStreamCloseReleasesBodyAfterEarlyTermination() throws IOException {
        TrackedInputStream body = new TrackedInputStream("data: first\n\ndata: second\n\n");
        CloseableIterableStream<String> iterable
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER);

        try (Stream<String> stream = iterable.stream()) {
            assertEquals("message:first", stream.findFirst().get());
            assertEquals(0, body.closes.get());
        }

        assertEquals(1, body.closes.get());
        iterable.close();
        assertEquals(1, body.closes.get());
    }

    @Test
    public void syncTerminalEventIsInclusiveAndClosesWithoutParsingTrailingBytes() throws IOException {
        String prefix = "data: first\n\nevent: done\ndata: last\n\n";
        byte[] bytes = Arrays.copyOf(prefix.getBytes(StandardCharsets.UTF_8), prefix.length() + 1);
        bytes[bytes.length - 1] = (byte) 0xFF;
        TrackedInputStream body = new TrackedInputStream(bytes, 1);
        AtomicInteger conversions = new AtomicInteger();

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), (event, data) -> {
                conversions.incrementAndGet();
                return event + ":" + data;
            }, event -> event.startsWith("done:"))) {
            Iterator<String> iterator = stream.iterator();
            assertEquals("message:first", iterator.next());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertEquals(0, body.closes.get());
            assertEquals("done:last", iterator.next());
            assertEquals(1, body.closes.get());
            assertFalse(iterator.hasNext());
            assertEquals(2, conversions.get());
            assertEquals(prefix.length(), body.position());
        }
    }

    @Test
    public void asyncTerminalEventStopsBeforeMalformedTrailingBytesInSameBuffer() {
        String prefix = "data: first\n\nevent: done\ndata: last\n\n";
        byte[] bytes = Arrays.copyOf(prefix.getBytes(StandardCharsets.UTF_8), prefix.length() + 1);
        bytes[bytes.length - 1] = (byte) 0xFF;
        AtomicInteger cancellations = new AtomicInteger();
        BinaryData body = reactiveBody(
            Flux.just(ByteBuffer.wrap(bytes)).concatWith(Flux.never()).doOnCancel(cancellations::incrementAndGet));

        StepVerifier
            .create(ServerSentEventStreams.toFlux(response(body), CONVERTER, event -> event.startsWith("done:")))
            .expectNext("message:first", "done:last")
            .verifyComplete();
        assertEquals(1, cancellations.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void asyncTerminalWaitsForDemandWithoutDecodingOrConvertingTrailingFrames(boolean separateBuffer) {
        byte[] events = "data: first\n\nevent: done\ndata: last\n\n".getBytes(StandardCharsets.UTF_8);
        byte[] trailingFrame = "event: trailing\ndata: must not convert\n\n".getBytes(StandardCharsets.UTF_8);
        byte[] trailingBytes = Arrays.copyOf(trailingFrame, trailingFrame.length + 3);
        trailingBytes[trailingFrame.length] = (byte) 0xFF;
        trailingBytes[trailingFrame.length + 1] = '\n';
        trailingBytes[trailingFrame.length + 2] = '\n';
        Flux<ByteBuffer> buffers;
        if (separateBuffer) {
            buffers = Flux.just(ByteBuffer.wrap(events), ByteBuffer.wrap(trailingBytes));
        } else {
            byte[] combined = Arrays.copyOf(events, events.length + trailingBytes.length);
            System.arraycopy(trailingBytes, 0, combined, events.length, trailingBytes.length);
            buffers = Flux.just(ByteBuffer.wrap(combined));
        }
        AtomicInteger conversions = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        BinaryData body = reactiveBody(buffers.concatWith(Flux.never()).doOnCancel(cancellations::incrementAndGet));
        Flux<String> stream = ServerSentEventStreams.toFlux(response(body), (event, data) -> {
            conversions.incrementAndGet();
            assertFalse("trailing".equals(event), "The trailing frame must not be converted.");
            return event + ":" + data;
        }, event -> event.startsWith("done:"));

        StepVerifier.create(stream, 1)
            .expectNext("message:first")
            .then(() -> assertEquals(1, conversions.get()))
            .thenRequest(1)
            .expectNext("done:last")
            .verifyComplete();

        assertEquals(2, conversions.get());
        assertEquals(1, cancellations.get());
    }

    @Test
    public void asyncTerminalEventTakesPrecedenceOverFollowingTransportError() {
        BinaryData body = reactiveBody(Flux.just(buffer("event: done\ndata: last\n\n"))
            .concatWith(Flux.error(new IOException("after terminal event"))));

        StepVerifier
            .create(ServerSentEventStreams.toFlux(response(body), CONVERTER, event -> event.startsWith("done:")))
            .expectNext("done:last")
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void asyncPendingTerminalTakesPrecedenceOverTransportError(boolean separateBuffer) {
        String first = "data: first\n\n";
        String terminal = "event: done\ndata: last\n\n";
        Flux<ByteBuffer> buffers
            = separateBuffer ? Flux.just(buffer(first), buffer(terminal)) : Flux.just(buffer(first + terminal));
        IOException failure = new IOException("after terminal event");
        AtomicInteger conversions = new AtomicInteger();
        BinaryData body = reactiveBody(buffers.concatWith(Flux.error(failure)));
        Flux<String> stream = ServerSentEventStreams.toFlux(response(body), (event, data) -> {
            conversions.incrementAndGet();
            return event + ":" + data;
        }, event -> event.startsWith("done:"));

        StepVerifier.create(stream, 1)
            .expectNext("message:first")
            .then(() -> assertEquals(1, conversions.get()))
            .thenRequest(1)
            .expectNext("done:last")
            .verifyComplete();

        assertEquals(2, conversions.get());
    }

    @Test
    public void asyncTransportErrorBeforeTerminalIsPropagatedWithBoundedDemand() {
        IOException failure = new IOException("before terminal event");
        AtomicInteger conversions = new AtomicInteger();
        BinaryData body = reactiveBody(Flux.just(buffer("data: first\n\n"))
            .concatWith(Flux.error(failure))
            .concatWith(Flux.just(buffer("event: done\ndata: unreachable\n\n"))));
        Flux<String> stream = ServerSentEventStreams.toFlux(response(body), (event, data) -> {
            conversions.incrementAndGet();
            return event + ":" + data;
        }, event -> event.startsWith("done:"));

        StepVerifier.create(stream, 1)
            .expectNext("message:first")
            .then(() -> assertEquals(1, conversions.get()))
            .thenRequest(1)
            .verifyErrorSatisfies(error -> assertSame(failure, error));

        assertEquals(1, conversions.get());
    }

    @Test
    public void nullConverterResultsAreSkippedWithoutTestingTerminalPredicate() throws IOException {
        String text = "data: skip\n\ndata: keep\n\n";
        BiFunction<String, String, String> converter = (event, data) -> "skip".equals(data) ? null : data;

        try (CloseableIterableStream<String> stream = ServerSentEventStreams
            .toIterableStream(response(BinaryData.fromString(text)), converter, event -> event.equals("keep"))) {
            Iterator<String> iterator = stream.iterator();
            assertEquals("keep", iterator.next());
            assertFalse(iterator.hasNext());
        }

        StepVerifier.create(ServerSentEventStreams.toFlux(response(BinaryData.fromString(text)), converter,
            event -> event.equals("keep"))).expectNext("keep").verifyComplete();
    }

    @Test
    public void converterFailureClosesSyncBodyAndCancelsAsyncBody() throws IOException {
        IllegalArgumentException failure = new IllegalArgumentException("malformed payload");
        BiFunction<String, String, String> converter = (event, data) -> {
            throw failure;
        };
        TrackedInputStream syncBody = new TrackedInputStream("data: payload\n\n");

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(syncBody)), converter)) {
            Iterator<String> iterator = stream.iterator();
            assertSame(failure, assertThrows(IllegalArgumentException.class, iterator::hasNext));
            assertEquals(1, syncBody.closes.get());
            assertFalse(iterator.hasNext());
        }

        AtomicInteger cancellations = new AtomicInteger();
        BinaryData asyncBody = reactiveBody(
            Flux.just(buffer("data: payload\n\n")).concatWith(Flux.never()).doOnCancel(cancellations::incrementAndGet));
        StepVerifier.create(ServerSentEventStreams.toFlux(response(asyncBody), converter))
            .verifyErrorSatisfies(error -> assertSame(failure, error));
        assertEquals(1, cancellations.get());
    }

    @Test
    public void terminalPredicateFailureClosesSyncBodyAndCancelsAsyncBody() throws IOException {
        IllegalArgumentException failure = new IllegalArgumentException("terminal predicate");
        TrackedInputStream syncBody = new TrackedInputStream("data: payload\n\n");

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(syncBody)), CONVERTER, event -> {
                throw failure;
            })) {
            assertSame(failure, assertThrows(IllegalArgumentException.class, () -> stream.iterator().hasNext()));
            assertEquals(1, syncBody.closes.get());
        }

        AtomicInteger cancellations = new AtomicInteger();
        BinaryData asyncBody = reactiveBody(
            Flux.just(buffer("data: payload\n\n")).concatWith(Flux.never()).doOnCancel(cancellations::incrementAndGet));
        StepVerifier.create(ServerSentEventStreams.toFlux(response(asyncBody), CONVERTER, event -> {
            throw failure;
        })).expectNext("message:payload").verifyErrorSatisfies(error -> assertSame(failure, error));
        assertEquals(1, cancellations.get());
    }

    @ParameterizedTest
    @MethodSource("malformedUtf8")
    public void malformedUtf8FailsAndReleasesBody(byte[] bytes) throws IOException {
        TrackedInputStream syncBody = new TrackedInputStream(bytes, 1);

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(syncBody)), CONVERTER)) {
            IllegalStateException error = assertThrows(IllegalStateException.class, () -> stream.iterator().hasNext());
            assertInstanceOf(CharacterCodingException.class, error.getCause());
            assertEquals(1, syncBody.closes.get());
        }

        AtomicInteger releases = new AtomicInteger();
        BinaryData asyncBody = reactiveBody(chunks(bytes, 1).doFinally(ignored -> releases.incrementAndGet()));
        StepVerifier.create(ServerSentEventStreams.toFlux(response(asyncBody), CONVERTER))
            .verifyErrorSatisfies(error -> {
                assertInstanceOf(IllegalStateException.class, error);
                assertInstanceOf(CharacterCodingException.class, error.getCause());
            });
        assertEquals(1, releases.get());
    }

    private static Stream<byte[]> malformedUtf8() {
        return Stream.of(new byte[] { 'd', 'a', 't', 'a', ':', (byte) 0xC3, '(', '\n', '\n' },
            new byte[] { 'd', 'a', 't', 'a', ':', (byte) 0xFF, '\n', '\n' },
            new byte[] { 'd', 'a', 't', 'a', ':', (byte) 0xE2, (byte) 0x82 });
    }

    @Test
    public void syncReadFailureClosesBodyAndPreservesCause() throws IOException {
        IOException failure = new IOException("read failed");
        AtomicInteger closes = new AtomicInteger();
        InputStream body = new InputStream() {
            @Override
            public int read() throws IOException {
                throw failure;
            }

            @Override
            public void close() {
                closes.incrementAndGet();
            }
        };

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER)) {
            UncheckedIOException error = assertThrows(UncheckedIOException.class, () -> stream.iterator().hasNext());
            assertSame(failure, error.getCause());
            assertEquals(1, closes.get());
        }

        assertEquals(1, closes.get());
    }

    @ParameterizedTest
    @MethodSource("closeFailures")
    public void syncReadFailureSuppressesCleanupFailure(Throwable closeFailure) throws IOException {
        IOException readFailure = new IOException("read failed");
        AtomicInteger closes = new AtomicInteger();
        InputStream body = new InputStream() {
            @Override
            public int read() throws IOException {
                throw readFailure;
            }

            @Override
            public void close() throws IOException {
                failClose(closeFailure, closes);
            }
        };

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER)) {
            Iterator<String> iterator = stream.iterator();
            UncheckedIOException error = assertThrows(UncheckedIOException.class, iterator::hasNext);
            assertSame(readFailure, error.getCause());
            assertEquals(1, error.getSuppressed().length);
            assertSame(closeFailure, error.getSuppressed()[0]);
            assertFalse(iterator.hasNext());
            assertEquals(1, closes.get());
        }

        assertEquals(1, closes.get());
    }

    @ParameterizedTest
    @MethodSource("closeFailures")
    public void syncMalformedUtf8FailureSuppressesCleanupFailure(Throwable closeFailure) throws IOException {
        AtomicInteger reads = new AtomicInteger();
        AtomicInteger closes = new AtomicInteger();
        byte[] bytes = { 'd', 'a', 't', 'a', ':', (byte) 0xFF, '\n', '\n' };
        InputStream body = failingCloseBody(bytes, closeFailure, reads, closes);

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER)) {
            Iterator<String> iterator = stream.iterator();
            IllegalStateException error = assertThrows(IllegalStateException.class, iterator::hasNext);
            assertInstanceOf(CharacterCodingException.class, error.getCause());
            assertEquals(1, error.getSuppressed().length);
            assertSame(closeFailure, error.getSuppressed()[0]);
            assertFalse(iterator.hasNext());
            assertTrue(reads.get() > 0);
            assertEquals(1, closes.get());
        }

        assertEquals(1, closes.get());
    }

    @ParameterizedTest
    @MethodSource("closeFailures")
    public void syncTerminalCleanupFailureIsReportedAfterDeliveryDuringIteration(Throwable closeFailure)
        throws IOException {
        for (boolean useNext : new boolean[] { false, true }) {
            AtomicInteger reads = new AtomicInteger();
            AtomicInteger closes = new AtomicInteger();
            InputStream body = failingCloseBody("event: done\ndata: last\n\n".getBytes(StandardCharsets.UTF_8),
                closeFailure, reads, closes);
            try (CloseableIterableStream<String> stream = ServerSentEventStreams.toIterableStream(
                response(BinaryData.fromStream(body)), CONVERTER, event -> event.startsWith("done:"))) {
                Iterator<String> iterator = stream.iterator();
                assertTrue(iterator.hasNext());
                assertEquals("done:last", iterator.next());
                assertEquals(1, closes.get());
                RuntimeException failure = assertThrows(RuntimeException.class, () -> {
                    if (useNext) {
                        iterator.next();
                    } else {
                        iterator.hasNext();
                    }
                });
                assertSame(closeFailure, closeFailure instanceof IOException ? failure.getCause() : failure);
                assertFalse(iterator.hasNext());
                assertEquals(1, closes.get());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("closeFailures")
    public void syncTerminalCleanupFailureIsReportedByExplicitClose(Throwable closeFailure) throws IOException {
        AtomicInteger reads = new AtomicInteger();
        AtomicInteger closes = new AtomicInteger();
        InputStream body = failingCloseBody("event: done\ndata: last\n\n".getBytes(StandardCharsets.UTF_8),
            closeFailure, reads, closes);
        CloseableIterableStream<String> stream = ServerSentEventStreams
            .toIterableStream(response(BinaryData.fromStream(body)), CONVERTER, event -> event.startsWith("done:"));
        assertEquals("done:last", stream.iterator().next());
        assertEquals(1, closes.get());
        assertSame(closeFailure, assertThrows(closeFailure.getClass(), stream::close));
        stream.close();
        assertEquals(1, closes.get());
    }

    @ParameterizedTest
    @MethodSource("closeFailures")
    public void syncTerminalCleanupFailureIsReportedByTryWithResources(Throwable closeFailure) {
        AtomicInteger reads = new AtomicInteger();
        AtomicInteger closes = new AtomicInteger();
        AtomicInteger delivered = new AtomicInteger();
        InputStream body = failingCloseBody("event: done\ndata: last\n\n".getBytes(StandardCharsets.UTF_8),
            closeFailure, reads, closes);
        assertSame(closeFailure, assertThrows(closeFailure.getClass(), () -> {
            try (CloseableIterableStream<String> stream = ServerSentEventStreams.toIterableStream(
                response(BinaryData.fromStream(body)), CONVERTER, event -> event.startsWith("done:"))) {
                assertEquals("done:last", stream.iterator().next());
                delivered.incrementAndGet();
                assertEquals(1, closes.get());
            }
        }));
        assertEquals(1, delivered.get());
        assertEquals(1, closes.get());
    }

    @ParameterizedTest
    @MethodSource("closeFailures")
    public void syncExplicitCloseFailureDoesNotReadOrRetryClosing(Throwable closeFailure) throws IOException {
        AtomicInteger reads = new AtomicInteger();
        AtomicInteger closes = new AtomicInteger();
        InputStream body
            = failingCloseBody("data: unread\n\n".getBytes(StandardCharsets.UTF_8), closeFailure, reads, closes);
        CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER);

        assertSame(closeFailure, assertThrows(closeFailure.getClass(), stream::close));
        stream.close();

        assertEquals(0, reads.get());
        assertEquals(1, closes.get());
        assertFalse(stream.iterator().hasNext());
    }

    @Test
    public void asyncTransportErrorPreservesPreviouslyEmittedEvent() {
        IOException failure = new IOException("transport failed");
        BinaryData body = reactiveBody(Flux.just(buffer("data: first\n\n")).concatWith(Flux.error(failure)));

        StepVerifier.create(ServerSentEventStreams.toFlux(response(body), CONVERTER))
            .expectNext("message:first")
            .verifyErrorSatisfies(error -> assertSame(failure, error));
    }

    @Test
    public void asyncConsumptionIsLazySingleSubscriptionAndHonorsCancellation() {
        AtomicInteger subscriptions = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        BinaryData body = reactiveBody(Flux.just(buffer("data: first\n\ndata: second\n\n"))
            .concatWith(Flux.never())
            .doOnSubscribe(ignored -> subscriptions.incrementAndGet())
            .doOnCancel(cancellations::incrementAndGet));
        Flux<String> stream = ServerSentEventStreams.toFlux(response(body), CONVERTER);

        assertEquals(0, subscriptions.get());
        StepVerifier.create(stream, 0).thenRequest(1).expectNext("message:first").thenCancel().verify();
        assertEquals(1, subscriptions.get());
        assertEquals(1, cancellations.get());
        StepVerifier.create(stream).verifyError(IllegalStateException.class);
        assertEquals(1, subscriptions.get());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "data: discarded", "data: discarded\n", "event: discarded\n\n", ": comment\n\n" })
    public void eofWithoutDispatchedEventCompletesAndCloses(String text) throws IOException {
        TrackedInputStream body = new TrackedInputStream(text);

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(BinaryData.fromStream(body)), CONVERTER)) {
            assertFalse(stream.iterator().hasNext());
            assertEquals(1, body.closes.get());
        }

        StepVerifier.create(ServerSentEventStreams.toFlux(response(BinaryData.fromString(text)), CONVERTER))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = { "text/event-stream", "TEXT/EVENT-STREAM", " text/event-stream ; charset=utf-8" })
    public void validContentTypesAreAccepted(String contentType) throws IOException {
        try (CloseableIterableStream<String> stream = ServerSentEventStreams
            .toIterableStream(response(200, contentType, BinaryData.fromString("data: value\n\n")), CONVERTER)) {
            assertEquals("message:value", stream.iterator().next());
        }

        StepVerifier
            .create(ServerSentEventStreams.toFlux(response(200, contentType, BinaryData.fromString("data: value\n\n")),
                CONVERTER))
            .expectNext("message:value")
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "application/json", "text/event-streaming", "text/event-stream, application/json" })
    public void invalidContentTypesReleaseBodies(String contentType) {
        assertInvalidResponseReleasesBody(200, contentType);
    }

    @ParameterizedTest
    @ValueSource(ints = { 201, 202, 400, 500 })
    public void invalidStatusCodesReleaseBodies(int statusCode) {
        assertInvalidResponseReleasesBody(statusCode, "text/event-stream");
    }

    @Test
    public void missingContentTypeReleasesBody() {
        assertInvalidResponseReleasesBody(200, null);
    }

    private static void assertInvalidResponseReleasesBody(int statusCode, String contentType) {
        TrackedInputStream body = new TrackedInputStream("data: unread\n\n");
        assertThrows(IllegalStateException.class, () -> ServerSentEventStreams
            .toIterableStream(response(statusCode, contentType, BinaryData.fromStream(body)), CONVERTER));
        assertEquals(0, body.reads.get());
        assertEquals(1, body.closes.get());

        AtomicInteger requests = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        BinaryData asyncBody = reactiveBody(Flux.<ByteBuffer>never()
            .doOnRequest(ignored -> requests.incrementAndGet())
            .doOnCancel(cancellations::incrementAndGet));
        Flux<String> stream = ServerSentEventStreams.toFlux(response(statusCode, contentType, asyncBody), CONVERTER);
        assertEquals(0, cancellations.get());
        StepVerifier.create(stream).verifyError(IllegalStateException.class);
        assertEquals(0, requests.get());
        assertEquals(1, cancellations.get());
    }

    @ParameterizedTest
    @MethodSource("closeFailures")
    public void syncValidationFailureSuppressesCleanupFailureWithoutReading(Throwable closeFailure) {
        for (int statusCode : new int[] { 200, 500 }) {
            AtomicInteger reads = new AtomicInteger();
            AtomicInteger closes = new AtomicInteger();
            InputStream body
                = failingCloseBody("data: unread\n\n".getBytes(StandardCharsets.UTF_8), closeFailure, reads, closes);

            IllegalStateException error = assertThrows(IllegalStateException.class, () -> ServerSentEventStreams
                .toIterableStream(response(statusCode, "application/json", BinaryData.fromStream(body)), CONVERTER));

            assertTrue(error.getMessage().startsWith("Expected a "));
            assertEquals(1, error.getSuppressed().length);
            if (closeFailure instanceof IOException) {
                assertInstanceOf(UncheckedIOException.class, error.getSuppressed()[0]);
                assertSame(closeFailure, error.getSuppressed()[0].getCause());
            } else {
                assertSame(closeFailure, error.getSuppressed()[0]);
            }
            assertEquals(0, reads.get());
            assertEquals(1, closes.get());
        }
    }

    @Test
    public void noContentResponseCompletesAndReleasesBodyWithoutReading() throws IOException {
        TrackedInputStream body = new TrackedInputStream("data: unread\n\n");
        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(204, null, BinaryData.fromStream(body)), CONVERTER)) {
            assertFalse(stream.iterator().hasNext());
            assertEquals(0, body.reads.get());
            assertEquals(1, body.closes.get());
        }
        assertEquals(1, body.closes.get());

        AtomicInteger requests = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        BinaryData asyncBody = reactiveBody(Flux.<ByteBuffer>never()
            .doOnRequest(ignored -> requests.incrementAndGet())
            .doOnCancel(cancellations::incrementAndGet));
        StepVerifier.create(ServerSentEventStreams.toFlux(response(204, null, asyncBody), CONVERTER)).verifyComplete();
        assertEquals(0, requests.get());
        assertEquals(1, cancellations.get());
    }

    @Test
    public void nullBodyIsOnlyAllowedForNoContentResponse() throws IOException {
        assertThrows(NullPointerException.class,
            () -> ServerSentEventStreams.toIterableStream(response(null), CONVERTER));
        StepVerifier.create(ServerSentEventStreams.toFlux(response(null), CONVERTER))
            .verifyError(NullPointerException.class);

        try (CloseableIterableStream<String> stream
            = ServerSentEventStreams.toIterableStream(response(204, null, null), CONVERTER)) {
            assertFalse(stream.iterator().hasNext());
        }
        StepVerifier.create(ServerSentEventStreams.toFlux(response(204, null, null), CONVERTER)).verifyComplete();
    }

    @Test
    public void nullFactoryArgumentsAreRejected() {
        Response<BinaryData> response = response(BinaryData.fromString(""));
        assertThrows(NullPointerException.class, () -> ServerSentEventStreams.toIterableStream(null, CONVERTER));
        assertThrows(NullPointerException.class, () -> ServerSentEventStreams.toIterableStream(response, null));
        assertThrows(NullPointerException.class,
            () -> ServerSentEventStreams.toIterableStream(response, CONVERTER, null));
        assertThrows(NullPointerException.class, () -> ServerSentEventStreams.toFlux(null, CONVERTER));
        assertThrows(NullPointerException.class, () -> ServerSentEventStreams.toFlux(response, null));
        assertThrows(NullPointerException.class, () -> ServerSentEventStreams.toFlux(response, CONVERTER, null));
    }

    private static String framedEvents() {
        return "\uFEFF: comment\r\nid: ignored\r\nretry: 12\r\nunknown: ignored\r\n"
            + "event: future.event\r\ndata: first\r\ndata:  second\r\ndata:\r\n\r\n"
            + "event:\ndata: caf\u00E9 \u6F22 \uD83D\uDE00\n\n" + "event: discarded\n\n" + "data: cr\r\r" + "data\n\n"
            + "data: unfinished";
    }

    private static List<String> expectedFramedEvents() {
        return Arrays.asList("future.event:first\n second\n", "message:caf\u00E9 \u6F22 \uD83D\uDE00", "message:cr",
            "message:");
    }

    private static Response<BinaryData> response(BinaryData body) {
        return response(200, "text/event-stream", body);
    }

    private static Response<BinaryData> response(int statusCode, String contentType, BinaryData body) {
        HttpHeaders headers = new HttpHeaders();
        if (contentType != null) {
            headers.set(HttpHeaderName.CONTENT_TYPE, contentType);
        }
        return new SimpleResponse<>(new HttpRequest(HttpMethod.GET, "https://example.com/events"), statusCode, headers,
            body);
    }

    private static BinaryData reactiveBody(Flux<ByteBuffer> body) {
        return BinaryData.fromFlux(body, null, false).block();
    }

    private static ByteBuffer buffer(String text) {
        return ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));
    }

    private static Flux<ByteBuffer> chunks(byte[] bytes, int chunkSize) {
        return Flux.range(0, (bytes.length + chunkSize - 1) / chunkSize)
            .map(index -> ByteBuffer.wrap(bytes, index * chunkSize,
                Math.min(chunkSize, bytes.length - index * chunkSize)));
    }

    private static Stream<Arguments> borrowedBufferCases() {
        return Stream.of(Arguments.of(false, false, true), Arguments.of(false, true, true),
            Arguments.of(true, false, true), Arguments.of(true, true, true), Arguments.of(false, false, false),
            Arguments.of(true, false, false));
    }

    private static ByteBuffer windowedBuffer(String text, boolean direct) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer
            = direct ? ByteBuffer.allocateDirect(bytes.length + 2) : ByteBuffer.allocate(bytes.length + 2);
        buffer.put((byte) 0xFF);
        buffer.put(bytes);
        buffer.put((byte) 0xFF);
        buffer.position(1);
        buffer.limit(1 + bytes.length);
        return buffer;
    }

    private static final class DemandSubscriber extends BaseSubscriber<String> {
        private final List<String> events = new ArrayList<>();
        private Throwable failure;
        private boolean completed;

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            request(1);
        }

        @Override
        protected void hookOnNext(String event) {
            events.add(event);
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            failure = throwable;
        }

        @Override
        protected void hookOnComplete() {
            completed = true;
        }
    }

    private static Stream<Throwable> closeFailures() {
        return Stream.of(new IOException("close failed"), new IllegalStateException("close failed"));
    }

    private static InputStream failingCloseBody(byte[] bytes, Throwable closeFailure, AtomicInteger reads,
        AtomicInteger closes) {
        return new ByteArrayInputStream(bytes) {
            @Override
            public synchronized int read() {
                reads.incrementAndGet();
                return super.read();
            }

            @Override
            public synchronized int read(byte[] buffer, int offset, int length) {
                reads.incrementAndGet();
                return super.read(buffer, offset, length);
            }

            @Override
            public void close() throws IOException {
                failClose(closeFailure, closes);
            }
        };
    }

    private static void failClose(Throwable closeFailure, AtomicInteger closes) throws IOException {
        closes.incrementAndGet();
        if (closeFailure instanceof IOException) {
            throw (IOException) closeFailure;
        }
        throw (RuntimeException) closeFailure;
    }

    private static final class TrackedInputStream extends ByteArrayInputStream {
        private final AtomicInteger reads = new AtomicInteger();
        private final AtomicInteger closes = new AtomicInteger();
        private final int chunkSize;

        private TrackedInputStream(String text) {
            this(text.getBytes(StandardCharsets.UTF_8), 8192);
        }

        private TrackedInputStream(byte[] bytes, int chunkSize) {
            super(bytes);
            this.chunkSize = chunkSize;
        }

        @Override
        public synchronized int read() {
            reads.incrementAndGet();
            return super.read();
        }

        @Override
        public synchronized int read(byte[] bytes, int offset, int length) {
            reads.incrementAndGet();
            return super.read(bytes, offset, Math.min(length, chunkSize));
        }

        @Override
        public void close() {
            closes.incrementAndGet();
        }

        private int position() {
            return pos;
        }
    }
}
