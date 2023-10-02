// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.util.io.IOUtils;
import com.typespec.core.util.mocking.MockAsynchronousFileChannel;
import com.typespec.core.util.mocking.MockMonoSink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.test.StepVerifier;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

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
            IOUtils.toAsynchronousByteChannel(channel, 0), null);

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

        StepVerifier.create(Mono.<Void>create(sink -> data.subscribe(
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

        StepVerifier.create(Mono.<Void>create(sink -> data.subscribe(
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

        StepVerifier.create(Mono.<Void>create(sink -> data.subscribe(
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
        StepVerifier.create(Mono.<Void>create(sink -> data.subscribe(
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

        return Stream.of(
            Arguments.of(Flux.create(sink -> {
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
            }), expectedData3)
        );
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

        AsynchronousByteChannelWriteSubscriber subscriber = new AsynchronousByteChannelWriteSubscriber(null,
            successTrackingSink);
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

        StepVerifier.create(Mono.<Void>create(sink ->
                onNextAfterTerminalState.subscribe(new AsynchronousByteChannelWriteSubscriber(null, sink))))
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

        StepVerifier.create(Mono.<Void>create(sink ->
                onNextAfterTerminalState.subscribe(new AsynchronousByteChannelWriteSubscriber(null, sink))))
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

        AsynchronousByteChannelWriteSubscriber subscriber = new AsynchronousByteChannelWriteSubscriber(
            IOUtils.toAsynchronousByteChannel(channel, 0), sink);
        subscriber.onSubscribe(new CallTrackingSubscription());
        subscriber.onNext(ByteBuffer.wrap(new byte[4096]));
        subscriber.onComplete();

        assertEquals(1, errorCallCount.get());
        assertEquals(0, successCallCount.get());
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
