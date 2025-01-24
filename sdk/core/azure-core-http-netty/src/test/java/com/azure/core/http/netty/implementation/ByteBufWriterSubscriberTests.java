// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.netty.mocking.MockMonoSink;
import com.azure.core.http.netty.mocking.WriteCountTrackingChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.core.validation.http.HttpValidatonUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link ByteBufWriteSubscriber}.
 */
@SuppressWarnings("resource")
public class ByteBufWriterSubscriberTests {
    /**
     * Tests that when multiple subscriptions are made to the subscriber all subscriptions after the first are ignored
     * and cancelled.
     */
    @Test
    public void multipleSubscribersCancelsSubscriptionsAfterTheFirstOne() {
        ByteBufWriteSubscriber byteBufWriteSubscriber = new ByteBufWriteSubscriber(null, null, null);

        CallTrackingSubscription subscription1 = new CallTrackingSubscription();
        CallTrackingSubscription subscription2 = new CallTrackingSubscription();

        byteBufWriteSubscriber.onSubscribe(subscription1);
        byteBufWriteSubscriber.onSubscribe(subscription2);

        assertEquals(1, subscription1.requestOneCalls.get());
        assertEquals(0, subscription1.cancelCalls.get());

        assertEquals(0, subscription2.requestOneCalls.get());
        assertEquals(1, subscription2.cancelCalls.get());
    }

    /**
     * Tests that empty {@link ByteBuf ByteBufs} are never written, or processed.
     */
    @Test
    public void emptyBuffersNeverWrite() {
        Flux<ByteBuf> data = Flux.create(sink -> {
            sink.next(Unpooled.EMPTY_BUFFER);
            sink.complete();
        });

        WriteCountTrackingChannel channel = new WriteCountTrackingChannel();
        StepVerifier
            .create(Mono.<Void>create(sink -> data.subscribe(new ByteBufWriteSubscriber(channel::write, sink, null))))
            .verifyComplete();

        assertEquals(0, channel.getWriteCount());
    }

    /**
     * Tests that all onNext emissions are handled before onComplete.
     */
    @Test
    public void allOnNextEmissionsAreHandledBeforeOnComplete() {
        WriteCountTrackingChannel channel = new WriteCountTrackingChannel();
        byte[] dataChunk = new byte[8192];
        ThreadLocalRandom.current().nextBytes(dataChunk);
        Flux<ByteBuf> data = Flux.create(sink -> {
            IntStream.range(0, 16).forEach(ignored -> sink.next(Unpooled.wrappedBuffer(dataChunk)));
            sink.complete();
        });

        StepVerifier
            .create(Mono.<Void>create(sink -> data.subscribe(new ByteBufWriteSubscriber(channel::write, sink, 8192L))))
            .verifyComplete();

        assertEquals(16, channel.getWriteCount());
    }

    /**
     * Tests that all onNext emissions are handled before onError.
     */
    @Test
    public void allOnNextEmissionsAreHandledBeforeOnError() {
        WriteCountTrackingChannel channel = new WriteCountTrackingChannel();
        byte[] dataChunk = new byte[8192];
        ThreadLocalRandom.current().nextBytes(dataChunk);
        Flux<ByteBuf> data = Flux.create(sink -> {
            IntStream.range(0, 16).forEach(ignored -> sink.next(Unpooled.wrappedBuffer(dataChunk)));
            sink.error(new IOException());
        });

        StepVerifier
            .create(Mono.<Void>create(sink -> data.subscribe(new ByteBufWriteSubscriber(channel::write, sink, 8192L))))
            .verifyError(IOException.class);

        // Last ByteBuf written isn't flushed as an error happened.
        assertEquals(15, channel.getWriteCount());
    }

    /**
     * Tests that a {@link ByteBuf} larger than the internal write buffer is written properly.
     */
    @ParameterizedTest
    @MethodSource("byteBufsLargerThanInternalBufferSupplier")
    public void byteBufsLargerThanInternalBufferWriteWithoutBuffering(Flux<ByteBuf> data, byte[] expectedData) {
        WriteCountTrackingChannel channel = new WriteCountTrackingChannel();
        StepVerifier
            .create(Mono.<Void>create(sink -> data.subscribe(new ByteBufWriteSubscriber(channel::write, sink, null))))
            .verifyComplete();

        assertArraysEqual(expectedData, channel.getDataWritten());
    }

    private static Stream<Arguments> byteBufsLargerThanInternalBufferSupplier() {
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
            sink.next(Unpooled.wrappedBuffer(smallChunk));
            sink.next(Unpooled.wrappedBuffer(mediumChunk));
            sink.next(Unpooled.wrappedBuffer(largeChunk));
            sink.complete();
        }), expectedData1),

            Arguments.of(Flux.create(sink -> {
                sink.next(Unpooled.wrappedBuffer(largeChunk));
                sink.next(Unpooled.wrappedBuffer(mediumChunk));
                sink.next(Unpooled.wrappedBuffer(smallChunk));
                sink.complete();
            }), expectedData2),

            Arguments.of(Flux.create(sink -> {
                sink.next(Unpooled.wrappedBuffer(smallChunk));
                sink.next(Unpooled.wrappedBuffer(largeChunk));
                sink.next(Unpooled.wrappedBuffer(mediumChunk));
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
        ByteBufWriteSubscriber byteBufWriteSubscriber = new ByteBufWriteSubscriber(null, sink, null);
        byteBufWriteSubscriber.onSubscribe(subscription);
        byteBufWriteSubscriber.onError(new IOException());
        byteBufWriteSubscriber.onError(new IllegalStateException());

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

        ByteBufWriteSubscriber byteBufWriteSubscriber = new ByteBufWriteSubscriber(null, successTrackingSink, null);
        byteBufWriteSubscriber.onComplete();
        byteBufWriteSubscriber.onComplete();

        assertEquals(1, successCallCount.get());
    }

    /**
     * Tests that onNext emissions are dropped after an onComplete is received.
     */
    @Test
    public void onNextAfterOnCompleteIsDropped() {
        AtomicInteger onNextDroppedCalledCount = new AtomicInteger();
        AtomicReference<ByteBuf> onNextDroppedByteBuf = new AtomicReference<>();
        Consumer<ByteBuf> onNextDroppedConsumer = byteBuf -> {
            onNextDroppedCalledCount.incrementAndGet();
            onNextDroppedByteBuf.set(byteBuf);
        };

        ByteBuf droppedByteBuf = Unpooled.wrappedBuffer(new byte[4096]);
        Flux<ByteBuf> onNextAfterTerminalState = Flux.<ByteBuf>create(sink -> {
            sink.complete();
            sink.next(droppedByteBuf);
        }).contextWrite(Context.of("reactor.onNextDropped.local", onNextDroppedConsumer));

        StepVerifier
            .create(Mono
                .<Void>create(sink -> onNextAfterTerminalState.subscribe(new ByteBufWriteSubscriber(null, sink, null))))
            .verifyComplete();

        assertEquals(1, onNextDroppedCalledCount.get());
        assertSame(droppedByteBuf, onNextDroppedByteBuf.get());
    }

    /**
     * Tests that onNext emissions are dropped after an onError is received.
     */
    @Test
    public void onNextAfterOnErrorIsDropped() {
        AtomicInteger onNextDroppedCalledCount = new AtomicInteger();
        AtomicReference<ByteBuf> onNextDroppedByteBuf = new AtomicReference<>();
        Consumer<ByteBuf> onNextDroppedConsumer = byteBuf -> {
            onNextDroppedCalledCount.incrementAndGet();
            onNextDroppedByteBuf.set(byteBuf);
        };

        ByteBuf droppedByteBuf = Unpooled.wrappedBuffer(new byte[4096]);
        Flux<ByteBuf> onNextAfterTerminalState = Flux.<ByteBuf>create(sink -> {
            sink.error(new IOException());
            sink.next(droppedByteBuf);
        }).contextWrite(Context.of("reactor.onNextDropped.local", onNextDroppedConsumer));

        StepVerifier
            .create(Mono
                .<Void>create(sink -> onNextAfterTerminalState.subscribe(new ByteBufWriteSubscriber(null, sink, null))))
            .verifyError(IOException.class);

        assertEquals(1, onNextDroppedCalledCount.get());
        assertSame(droppedByteBuf, onNextDroppedByteBuf.get());
    }

    /**
     * Tests that if the final data written during onComplete errors that an onError is emitted instead of onComplete.
     */
    @Test
    public void errorWritingDuringOnCompleteResultsInOnError() {
        WriteCountTrackingChannel channel = new WriteCountTrackingChannel() {
            @Override
            public int write(ByteBuffer src) throws IOException {
                throw new IOException();
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

        ByteBufWriteSubscriber byteBufWriteSubscriber = new ByteBufWriteSubscriber(channel::write, sink, null);
        byteBufWriteSubscriber.onSubscribe(new CallTrackingSubscription());
        byteBufWriteSubscriber.onNext(Unpooled.wrappedBuffer(new byte[4096]));
        byteBufWriteSubscriber.onComplete();

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
}
