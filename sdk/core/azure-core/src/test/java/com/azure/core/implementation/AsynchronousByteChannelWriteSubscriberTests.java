// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.io.IOUtils;
import com.azure.core.util.mocking.MockAsynchronousFileChannel;
import com.azure.core.util.mocking.MockMonoSink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.test.StepVerifier;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link AsynchronousByteChannelWriteSubscriber}.
 */
@SuppressWarnings("resource")
public class AsynchronousByteChannelWriteSubscriberTests {
    @Test
    public void multipleSubscriptionsCancelsLaterSubscriptions() {
        AsynchronousFileChannel channel = new MockAsynchronousFileChannel() {
            @Override
            public <A> void write(ByteBuffer src, long position, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
                int remaining = src.remaining();
                src.position(src.position() + remaining);
                handler.completed(remaining, attachment);
            }
        };

        AsynchronousByteChannelWriteSubscriber fileWriteSubscriber = new AsynchronousByteChannelWriteSubscriber(
            IOUtils.toAsynchronousByteChannel(channel, 0), new MockMonoSink<>());

        CallTrackingSubscription subscription1 = new CallTrackingSubscription();
        CallTrackingSubscription subscription2 = new CallTrackingSubscription();

        fileWriteSubscriber.onSubscribe(subscription1);
        fileWriteSubscriber.onSubscribe(subscription2);

        assertEquals(1, subscription1.requestOneCalls.get());
        assertEquals(0, subscription1.cancelCalls.get());

        assertEquals(0, subscription2.requestOneCalls.get());
        assertEquals(1, subscription2.cancelCalls.get());
    }

    /**
     * Tests that empty {@link ByteBuffer ByteBufs} are never written, or processed.
     */
    @Test
    public void emptyBuffersNeverWrite() {
        Flux<ByteBuffer> data = Flux.create(sink -> {
            sink.next(ByteBuffer.allocate(0));
            sink.complete();
        });

        WriteCountTrackingChannel channel = new WriteCountTrackingChannel(new byte[4096]);

        StepVerifier
            .create(Mono.<Void>create(sink -> data.subscribe(
                new AsynchronousByteChannelWriteSubscriber(IOUtils.toAsynchronousByteChannel(channel, 0), sink))))
            .verifyComplete();

        assertEquals(0, channel.writeCount);
    }

    /**
     * Tests that all onNext emissions are handled before onComplete.
     */
    @Test
    public void allOnNextEmissionsAreHandledBeforeOnComplete() {
        WriteCountTrackingChannel channel = new WriteCountTrackingChannel(new byte[8192 * 16]);
        byte[] dataChunk = new byte[8192];
        ThreadLocalRandom.current().nextBytes(dataChunk);
        Flux<ByteBuffer> data = Flux.create(sink -> {
            IntStream.range(0, 16).forEach(ignored -> sink.next(ByteBuffer.wrap(dataChunk)));
            sink.complete();
        });

        StepVerifier
            .create(Mono.<Void>create(sink -> data.subscribe(
                new AsynchronousByteChannelWriteSubscriber(IOUtils.toAsynchronousByteChannel(channel, 0), sink))))
            .verifyComplete();

        assertEquals(16, channel.writeCount);
    }

    /**
     * Tests that all onNext emissions are handled before onError.
     */
    @Test
    public void allOnNextEmissionsAreHandledBeforeOnError() {
        WriteCountTrackingChannel channel = new WriteCountTrackingChannel(new byte[8192 * 16]);
        byte[] dataChunk = new byte[8192];
        ThreadLocalRandom.current().nextBytes(dataChunk);
        Flux<ByteBuffer> data = Flux.create(sink -> {
            IntStream.range(0, 16).forEach(ignored -> sink.next(ByteBuffer.wrap(dataChunk)));
            sink.error(new IOException());
        });

        StepVerifier
            .create(Mono.<Void>create(sink -> data.subscribe(
                new AsynchronousByteChannelWriteSubscriber(IOUtils.toAsynchronousByteChannel(channel, 0), sink))))
            .verifyError(IOException.class);

        assertEquals(16, channel.writeCount);
    }

    /**
     * Tests that a {@link ByteBuffer} are written correctly.
     */
    @ParameterizedTest
    @MethodSource("writtenDataValidationSupplier")
    public void writtenDataValidation(Flux<ByteBuffer> data, byte[] expectedData) {
        byte[] actualData = new byte[expectedData.length];
        WriteCountTrackingChannel channel = new WriteCountTrackingChannel(actualData);
        StepVerifier
            .create(Mono.<Void>create(sink -> data.subscribe(
                new AsynchronousByteChannelWriteSubscriber(IOUtils.toAsynchronousByteChannel(channel, 0), sink))))
            .verifyComplete();

        assertArrayEquals(expectedData, actualData);
    }

    private static Stream<Arguments> writtenDataValidationSupplier() {
        byte[] smallChunk = new byte[4196];
        ThreadLocalRandom.current().nextBytes(smallChunk);
        byte[] mediumChunk = new byte[16392];
        ThreadLocalRandom.current().nextBytes(mediumChunk);
        byte[] largeChunk = new byte[131136];
        ThreadLocalRandom.current().nextBytes(largeChunk);

        byte[] expectedData1 = new byte[smallChunk.length + mediumChunk.length + largeChunk.length];
        System.arraycopy(smallChunk, 0, expectedData1, 0, smallChunk.length);
        System.arraycopy(mediumChunk, 0, expectedData1, smallChunk.length, mediumChunk.length);
        System.arraycopy(largeChunk, 0, expectedData1, smallChunk.length + mediumChunk.length, largeChunk.length);

        byte[] expectedData2 = new byte[smallChunk.length + mediumChunk.length + largeChunk.length];
        System.arraycopy(largeChunk, 0, expectedData2, 0, largeChunk.length);
        System.arraycopy(mediumChunk, 0, expectedData2, largeChunk.length, mediumChunk.length);
        System.arraycopy(smallChunk, 0, expectedData2, largeChunk.length + mediumChunk.length, smallChunk.length);

        byte[] expectedData3 = new byte[smallChunk.length + mediumChunk.length + largeChunk.length];
        System.arraycopy(smallChunk, 0, expectedData3, 0, smallChunk.length);
        System.arraycopy(largeChunk, 0, expectedData3, smallChunk.length, largeChunk.length);
        System.arraycopy(mediumChunk, 0, expectedData3, smallChunk.length + largeChunk.length, mediumChunk.length);

        return Stream.of(Arguments.of(Flux.create(sink -> {
            sink.next(ByteBuffer.wrap(smallChunk));
            sink.next(ByteBuffer.wrap(mediumChunk));
            sink.next(ByteBuffer.wrap(largeChunk));
            sink.complete();
        }), expectedData1),

            Arguments.of(Flux.create(sink -> {
                sink.next(ByteBuffer.wrap(largeChunk));
                sink.next(ByteBuffer.wrap(mediumChunk));
                sink.next(ByteBuffer.wrap(smallChunk));
                sink.complete();
            }), expectedData2),

            Arguments.of(Flux.create(sink -> {
                sink.next(ByteBuffer.wrap(smallChunk));
                sink.next(ByteBuffer.wrap(largeChunk));
                sink.next(ByteBuffer.wrap(mediumChunk));
                sink.complete();
            }), expectedData3));
    }

    @Test
    public void onErrorAfterErrorIsDropped() {
        AtomicInteger onErrorDroppedCalledCount = new AtomicInteger();
        AtomicReference<Throwable> onErrorDroppedError = new AtomicReference<>();
        Consumer<Throwable> onErrorDroppedConsumer = throwable -> {
            onErrorDroppedCalledCount.incrementAndGet();
            onErrorDroppedError.set(throwable);
        };
        Context context = Context.of("reactor.onErrorDropped.local", onErrorDroppedConsumer);
        MonoSink<Void> sink = new MockMonoSink<Void>() {
            @Override
            public ContextView contextView() {
                return context;
            }
        };

        CallTrackingSubscription subscription = new CallTrackingSubscription();
        AsynchronousByteChannelWriteSubscriber subscriber = new AsynchronousByteChannelWriteSubscriber(null, sink);
        subscriber.onSubscribe(subscription);
        subscriber.onError(new IOException());
        subscriber.onError(new IllegalStateException());

        assertEquals(1, subscription.cancelCalls.get());
        assertEquals(1, onErrorDroppedCalledCount.get());
        assertInstanceOf(IllegalStateException.class, onErrorDroppedError.get());
    }

    /**
     * Tests that once onComplete is called it won't emit success again.
     */
    @Test
    public void onCompleteAfterOnCompleteIsIgnored() {
        AtomicInteger successCallCount = new AtomicInteger();
        MonoSink<Void> successTrackingSink = new MockMonoSink<Void>() {
            @Override
            public void success() {
                successCallCount.incrementAndGet();
            }
        };

        AsynchronousByteChannelWriteSubscriber subscriber
            = new AsynchronousByteChannelWriteSubscriber(null, successTrackingSink);
        subscriber.onComplete();
        subscriber.onComplete();

        assertEquals(1, successCallCount.get());
    }

    /**
     * Tests that onNext emissions are dropped after an onComplete is received.
     */
    @Test
    public void onNextAfterOnCompleteIsDropped() {
        AtomicInteger onNextDroppedCalledCount = new AtomicInteger();
        AtomicReference<ByteBuffer> onNextDroppedByteBuffer = new AtomicReference<>();
        Consumer<ByteBuffer> onNextDroppedConsumer = byteBuf -> {
            onNextDroppedCalledCount.incrementAndGet();
            onNextDroppedByteBuffer.set(byteBuf);
        };

        ByteBuffer droppedByteBuffer = ByteBuffer.wrap(new byte[4096]);
        Flux<ByteBuffer> onNextAfterTerminalState = Flux.<ByteBuffer>create(sink -> {
            sink.complete();
            sink.next(droppedByteBuffer);
        }).contextWrite(Context.of("reactor.onNextDropped.local", onNextDroppedConsumer));

        StepVerifier
            .create(Mono.<Void>create(
                sink -> onNextAfterTerminalState.subscribe(new AsynchronousByteChannelWriteSubscriber(null, sink))))
            .verifyComplete();

        assertEquals(1, onNextDroppedCalledCount.get());
        assertSame(droppedByteBuffer, onNextDroppedByteBuffer.get());
    }

    /**
     * Tests that onNext emissions are dropped after an onError is received.
     */
    @Test
    public void onNextAfterOnErrorIsDropped() {
        AtomicInteger onNextDroppedCalledCount = new AtomicInteger();
        AtomicReference<ByteBuffer> onNextDroppedByteBuffer = new AtomicReference<>();
        Consumer<ByteBuffer> onNextDroppedConsumer = byteBuf -> {
            onNextDroppedCalledCount.incrementAndGet();
            onNextDroppedByteBuffer.set(byteBuf);
        };

        ByteBuffer droppedByteBuffer = ByteBuffer.wrap(new byte[4096]);
        Flux<ByteBuffer> onNextAfterTerminalState = Flux.<ByteBuffer>create(sink -> {
            sink.error(new IOException());
            sink.next(droppedByteBuffer);
        }).contextWrite(Context.of("reactor.onNextDropped.local", onNextDroppedConsumer));

        StepVerifier
            .create(Mono.<Void>create(
                sink -> onNextAfterTerminalState.subscribe(new AsynchronousByteChannelWriteSubscriber(null, sink))))
            .verifyError(IOException.class);

        assertEquals(1, onNextDroppedCalledCount.get());
        assertSame(droppedByteBuffer, onNextDroppedByteBuffer.get());
    }

    /**
     * Tests that if the final data written during onComplete errors that an onError is emitted instead of onComplete.
     */
    @Test
    public void errorWritingDuringOnCompleteResultsInOnError() {
        WriteCountTrackingChannel channel = new WriteCountTrackingChannel(new byte[4096]) {
            @Override
            public <A> void write(ByteBuffer src, long position, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
                handler.failed(new IOException(), attachment);
            }

            @Override
            public Future<Integer> write(ByteBuffer src, long position) {
                return new CompletableFuture<Integer>() {
                    @Override
                    public Integer get() throws InterruptedException, ExecutionException {
                        throw new ExecutionException(new IOException());
                    }
                };
            }
        };

        AtomicInteger successCallCount = new AtomicInteger();
        AtomicInteger errorCallCount = new AtomicInteger();
        MonoSink<Void> sink = new MockMonoSink<Void>() {
            @Override
            public void success() {
                successCallCount.incrementAndGet();
            }

            @Override
            public void error(Throwable e) {
                errorCallCount.incrementAndGet();
            }
        };

        AsynchronousByteChannelWriteSubscriber subscriber
            = new AsynchronousByteChannelWriteSubscriber(IOUtils.toAsynchronousByteChannel(channel, 0), sink);
        subscriber.onSubscribe(new CallTrackingSubscription());
        subscriber.onNext(ByteBuffer.wrap(new byte[4096]));
        subscriber.onComplete();

        assertEquals(1, errorCallCount.get());
        assertEquals(0, successCallCount.get());
    }

    @Test
    public void deferredPartialWritesRespectDemandAndDelayCompletion() {
        DeferredWriteChannel channel = new DeferredWriteChannel();
        AtomicInteger emitted = new AtomicInteger();
        AtomicBoolean completed = new AtomicBoolean();
        Flux<ByteBuffer> content
            = Flux.just(ByteBuffer.wrap(new byte[] { 1, 2, 3 }), ByteBuffer.wrap(new byte[] { 4, 5, 6 }))
                .doOnNext(ignored -> emitted.incrementAndGet());

        StepVerifier.create(FluxUtil.writeFile(content, channel).doOnSuccess(ignored -> completed.set(true)))
            .then(() -> {
                assertEquals(1, emitted.get());
                assertFalse(completed.get());
                channel.completeWrite();
                assertEquals(1, emitted.get());
                channel.completeWrite();
                assertEquals(2, emitted.get());
                assertFalse(completed.get());
                channel.completeWrite();
                assertFalse(completed.get());
                channel.completeWrite();
            })
            .expectComplete()
            .verify(Duration.ofSeconds(5));

        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6 }, channel.output.toByteArray());
        assertTrue(channel.isOpen());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void deferredWritesOwnProducerStorage(boolean direct) {
        DeferredWriteChannel channel = new DeferredWriteChannel();
        ByteBuffer producer = direct ? ByteBuffer.allocateDirect(8) : ByteBuffer.allocate(8);
        producer.put(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
        producer.position(2);
        producer.limit(6);
        Flux<ByteBuffer> content = Flux.just(producer.asReadOnlyBuffer());

        StepVerifier.create(FluxUtil.writeFile(content, channel)).then(() -> {
            assertNotNull(channel.pending.get());
            producer.clear();
            while (producer.hasRemaining()) {
                producer.put((byte) 99);
            }
            channel.completeWrite();
            channel.completeWrite();
        }).expectComplete().verify(Duration.ofSeconds(5));
        assertArrayEquals(new byte[] { 2, 3, 4, 5 }, channel.output.toByteArray());
    }

    @Test
    public void pendingWriteFailureAfterSourceCompletionIsPropagated() {
        DeferredWriteChannel channel = new DeferredWriteChannel();
        IOException expected = new IOException("Write failed");
        StepVerifier.create(FluxUtil.writeFile(Flux.just(ByteBuffer.wrap(new byte[] { 1 })), channel))
            .then(() -> channel.failWrite(expected))
            .expectErrorMatches(error -> error == expected)
            .verify(Duration.ofSeconds(5));
    }

    @Test
    public void sourceErrorWaitsForPendingWrite() {
        DeferredWriteChannel channel = new DeferredWriteChannel();
        IOException expected = new IOException("Source failed");
        Flux<ByteBuffer> content = Flux.just(ByteBuffer.wrap(new byte[] { 1, 2, 3 })).concatWith(Flux.error(expected));

        StepVerifier.create(FluxUtil.writeFile(content, channel))
            .then(channel::completeWrite)
            .then(channel::completeWrite)
            .expectErrorMatches(error -> error == expected)
            .verify(Duration.ofSeconds(5));
        assertArrayEquals(new byte[] { 1, 2, 3 }, channel.output.toByteArray());
    }

    @Test
    public void inlinePartialWritesDoNotRecurse() {
        AtomicInteger writes = new AtomicInteger();
        MockAsynchronousFileChannel channel = new MockAsynchronousFileChannel() {
            @Override
            public <A> void write(ByteBuffer src, long position, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
                src.get();
                writes.incrementAndGet();
                handler.completed(1, attachment);
            }
        };
        StepVerifier.create(FluxUtil.writeFile(Flux.just(ByteBuffer.allocate(65536)), channel))
            .expectComplete()
            .verify(Duration.ofSeconds(5));
        assertEquals(65536, writes.get());
    }

    @Test
    public void cancellationStopsSourceWithoutClosingCallerChannel() {
        DeferredWriteChannel channel = new DeferredWriteChannel();
        AtomicBoolean cancelled = new AtomicBoolean();
        Flux<ByteBuffer> content = Flux.just(ByteBuffer.wrap(new byte[] { 1, 2, 3 }))
            .concatWith(Flux.never())
            .doOnCancel(() -> cancelled.set(true));
        CompletableFuture<Void> transfer = FluxUtil.writeFile(content, channel).toFuture();

        assertFalse(transfer.isDone());
        assertTrue(transfer.cancel(true));
        assertTrue(cancelled.get());
        channel.completeWrite();
        assertNull(channel.pending.get());
        assertTrue(channel.isOpen());
    }

    @Test
    public void cancellationBeforeSubscriptionCancelsSource() {
        AsynchronousByteChannelWriteSubscriber subscriber
            = new AsynchronousByteChannelWriteSubscriber(null, new MockMonoSink<>());
        subscriber.dispose();
        CallTrackingSubscription subscription = new CallTrackingSubscription();
        subscriber.onSubscribe(subscription);
        assertEquals(1, subscription.cancelCalls.get());
        assertEquals(0, subscription.requestOneCalls.get());
    }

    private static final class DeferredWriteChannel extends MockAsynchronousFileChannel {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final AtomicReference<Runnable> pending = new AtomicReference<>();
        private Consumer<Throwable> fail;

        @Override
        public <A> void write(ByteBuffer src, long position, A attachment,
            CompletionHandler<Integer, ? super A> handler) {
            assertNull(pending.get(), "Only one write may be pending");
            pending.set(() -> {
                int count = Math.min(2, src.remaining());
                for (int i = 0; i < count; i++) {
                    output.write(src.get());
                }
                handler.completed(count, attachment);
            });
            fail = error -> handler.failed(error, attachment);
        }

        @Override
        public Future<Integer> write(ByteBuffer src, long position) {
            throw new AssertionError("Asynchronous writes must use a completion handler");
        }

        private void completeWrite() {
            Runnable completion = pending.getAndSet(null);
            assertNotNull(completion);
            completion.run();
        }

        private void failWrite(Throwable error) {
            assertNotNull(pending.getAndSet(null));
            fail.accept(error);
        }

        @Override
        public boolean isOpen() {
            return true;
        }
    }

    private static final class CallTrackingSubscription implements Subscription {
        private final AtomicInteger requestOneCalls = new AtomicInteger();
        private final AtomicInteger cancelCalls = new AtomicInteger();

        @Override
        public void request(long n) {
            if (n == 1) {
                requestOneCalls.incrementAndGet();
            }
        }

        @Override
        public void cancel() {
            cancelCalls.incrementAndGet();
        }
    }

    private static class WriteCountTrackingChannel extends MockAsynchronousFileChannel {
        private int writeCount = 0;

        WriteCountTrackingChannel(byte[] writeToByteArray) {
            super(writeToByteArray);
        }

        @Override
        public <A> void write(ByteBuffer src, long position, A attachment,
            CompletionHandler<Integer, ? super A> handler) {
            writeCount++;
            super.write(src, position, attachment, handler);
        }

        @Override
        public Future<Integer> write(ByteBuffer src, long position) {
            writeCount++;
            return super.write(src, position);
        }
    }
}
