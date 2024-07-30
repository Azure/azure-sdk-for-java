// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import com.azure.core.amqp.implementation.WindowedSubscriber.EnqueueResult;
import com.azure.core.amqp.implementation.WindowedSubscriber.WindowedSubscriberOptions;

public final class WindowedSubscriberFluxWindowTest {
    private static final HashMap<String, Object> EMPTY_LOGGING_CONTEXT = new HashMap<>(0);
    private static final String TERMINATED_MESSAGE = "client terminated";

    @Test
    public void shouldCloseWindowForWorkArrivedAfterUpstreamError() {
        final int windowSize = 1;
        final Duration windowTimeout = Duration.ofSeconds(10);
        final RuntimeException upstreamError = new RuntimeException("connection-error");
        final TestPublisher<Integer> upstream = TestPublisher.create();
        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);
        upstream.error(upstreamError);

        final EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> windowFlux = r.getWindowFlux();

        StepVerifier.create(windowFlux).verifyErrorSatisfies(e -> {
            final String message = e.getMessage();
            Assertions.assertNotNull(message);
            assertUpstreamErrorMessage(message);
            Assertions.assertEquals(upstreamError, e.getCause());
        });
        upstream.assertNotCancelled();

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertFalse(work.hasTimedOut());
        Assertions.assertFalse(work.isCanceled());
    }

    @Test
    public void shouldCloseStreamingWindowOnUpstreamError() {
        final int windowSize = 10;
        final Duration windowTimeout = Duration.ofSeconds(60);
        final RuntimeException upstreamError = new RuntimeException("connection-error");
        final Upstream<Integer> upstream = new Upstream<>();
        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> window0Flux = r0.getWindowFlux();
        final Flux<Integer> window1Flux = r1.getWindowFlux();

        StepVerifier.create(window0Flux)
            .then(() -> upstream.next(1))
            .then(() -> upstream.next(2))
            .then(() -> upstream.error(upstreamError))
            .expectNext(1, 2)
            .verifyErrorSatisfies(e0 -> {
                final String message0 = e0.getMessage();
                Assertions.assertNotNull(message0);
                assertUpstreamErrorMessage(message0);
                Assertions.assertEquals(upstreamError, e0.getCause());
            });

        StepVerifier.create(window1Flux).expectNextCount(0).verifyErrorSatisfies(e1 -> {
            final String message1 = e1.getMessage();
            Assertions.assertNotNull(message1);
            assertUpstreamErrorMessage(message1);
            Assertions.assertEquals(upstreamError, e1.getCause());
        });

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertFalse(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertFalse(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldCleanCloseStreamingWindowOnUpstreamError() {
        final int windowSize = 10;
        final Duration windowTimeout = Duration.ofSeconds(60);
        final RuntimeException upstreamError = new RuntimeException("connection-error");
        final Upstream<Integer> upstream = new Upstream<>();
        final WindowedSubscriberOptions<Integer> options
            = new WindowedSubscriberOptions<Integer>().cleanCloseStreamingWindowOnTerminate();
        final WindowedSubscriber<Integer> subscriber = createSubscriber(options);
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> window0Flux = r0.getWindowFlux();
        final Flux<Integer> window1Flux = r1.getWindowFlux();

        StepVerifier.create(window0Flux)
            .then(() -> upstream.next(1))
            .then(() -> upstream.next(2))
            .then(() -> upstream.error(upstreamError))
            .expectNext(1, 2)
            // Clean close since window0Flux was in 'streaming state' (aka 'streaming window').
            .verifyComplete();

        StepVerifier.create(window1Flux)
            .expectNextCount(0)
            // Any work (window1Flux) enqueued after the closure of 'streaming window' will be closed with error.
            .verifyErrorSatisfies(e1 -> {
                final String message1 = e1.getMessage();
                Assertions.assertNotNull(message1);
                assertUpstreamErrorMessage(message1);
                Assertions.assertEquals(upstreamError, e1.getCause());
            });

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertFalse(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertFalse(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldCloseWindowForWorkArrivedAfterUpstreamCompletion() {
        final int windowSize = 1;
        final Duration windowTimeout = Duration.ofSeconds(10);
        final TestPublisher<Integer> upstream = TestPublisher.create();
        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);
        upstream.complete();

        final EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> windowFlux = r.getWindowFlux();

        StepVerifier.create(windowFlux).verifyErrorSatisfies(e -> {
            final String message = e.getMessage();
            Assertions.assertNotNull(message);
            assertUpstreamCompletedMessage(message);
        });
        upstream.assertNotCancelled();

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertFalse(work.hasTimedOut());
        Assertions.assertFalse(work.isCanceled());
    }

    @Test
    public void shouldCloseStreamingWindowOnUpstreamCompletion() {
        final int windowSize = 10;
        final Duration windowTimeout = Duration.ofSeconds(60);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> window0Flux = r0.getWindowFlux();
        final Flux<Integer> window1Flux = r1.getWindowFlux();

        StepVerifier.create(window0Flux)
            .then(() -> upstream.next(1))
            .then(() -> upstream.next(2))
            .then(() -> upstream.complete())
            .expectNext(1, 2)
            .verifyErrorSatisfies(e0 -> {
                final String message0 = e0.getMessage();
                Assertions.assertNotNull(message0);
                assertUpstreamCompletedMessage(message0);
            });

        StepVerifier.create(window1Flux).expectNextCount(0).verifyErrorSatisfies(e1 -> {
            final String message1 = e1.getMessage();
            Assertions.assertNotNull(message1);
            assertUpstreamCompletedMessage(message1);
        });

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertFalse(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertFalse(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldCleanCloseStreamingWindowOnUpstreamCompletion() {
        final int windowSize = 10;
        final Duration windowTimeout = Duration.ofSeconds(60);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriberOptions<Integer> options
            = new WindowedSubscriberOptions<Integer>().cleanCloseStreamingWindowOnTerminate();
        final WindowedSubscriber<Integer> subscriber = createSubscriber(options);
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> window0Flux = r0.getWindowFlux();
        final Flux<Integer> window1Flux = r1.getWindowFlux();

        StepVerifier.create(window0Flux)
            .then(() -> upstream.next(1))
            .then(() -> upstream.next(2))
            .then(() -> upstream.complete())
            // Clean close since window0Flux was in 'streaming state' (aka 'streaming window').
            .expectNext(1, 2)
            .verifyComplete();

        StepVerifier.create(window1Flux)
            .expectNextCount(0)
            // Any work (window1Flux) enqueued after the closure of 'streaming window' will be closed with error.
            .verifyErrorSatisfies(e1 -> {
                final String message1 = e1.getMessage();
                Assertions.assertNotNull(message1);
                assertUpstreamCompletedMessage(message1);
            });

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertFalse(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertFalse(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldCloseWindowForWorkArrivedAfterDownstreamCancel() {
        final int windowSize = 1;
        final Duration windowTimeout = Duration.ofSeconds(10);
        final Flux<Integer> upstream = Flux.never();
        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribeWith(subscriber).cancel();

        final EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> windowFlux = r.getWindowFlux();
        StepVerifier.create(windowFlux).verifyErrorSatisfies(e -> {
            final String message = e.getMessage();
            Assertions.assertNotNull(message);
            assertDownstreamCanceledMessage(message);
        });

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertFalse(work.hasTimedOut());
        Assertions.assertFalse(work.isCanceled());
    }

    @Test
    public void shouldCloseStreamingWindowOnDownstreamCancel() {
        final int windowSize = 10;
        final Duration windowTimeout = Duration.ofSeconds(60);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        final Disposable disposable = upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> window0Flux = r0.getWindowFlux();
        final Flux<Integer> window1Flux = r1.getWindowFlux();

        StepVerifier.create(window0Flux)
            .then(() -> upstream.next(1))
            .then(() -> upstream.next(2))
            .then(() -> disposable.dispose())
            .expectNext(1, 2)
            .verifyErrorSatisfies(e0 -> {
                final String message0 = e0.getMessage();
                Assertions.assertNotNull(message0);
                assertDownstreamCanceledMessage(message0);
            });

        StepVerifier.create(window1Flux).expectNextCount(0).verifyErrorSatisfies(e1 -> {
            final String message1 = e1.getMessage();
            Assertions.assertNotNull(message1);
            assertDownstreamCanceledMessage(message1);
        });

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertFalse(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertFalse(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldCleanCloseStreamingWindowOnDownstreamCancel() {
        final int windowSize = 10;
        final Duration windowTimeout = Duration.ofSeconds(60);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriberOptions<Integer> options
            = new WindowedSubscriberOptions<Integer>().cleanCloseStreamingWindowOnTerminate();
        final WindowedSubscriber<Integer> subscriber = createSubscriber(options);
        final Disposable disposable = upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> window0Flux = r0.getWindowFlux();
        final Flux<Integer> window1Flux = r1.getWindowFlux();

        StepVerifier.create(window0Flux)
            .then(() -> upstream.next(1))
            .then(() -> upstream.next(2))
            .then(() -> disposable.dispose())
            .expectNext(1, 2)
            // Clean close since window0Flux was in 'streaming state' (aka 'streaming window').
            .verifyComplete();

        StepVerifier.create(window1Flux)
            .expectNextCount(0)
            // Any work (window1Flux) enqueued after the closure of 'streaming window' will be closed with error.
            .verifyErrorSatisfies(e1 -> {
                final String message1 = e1.getMessage();
                Assertions.assertNotNull(message1);
                assertDownstreamCanceledMessage(message1);
            });

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertFalse(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertFalse(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldCloseStreamingWindowOnceDemandReceived() {
        final int window0Size = 5;
        final int window1Size = 3;
        final Duration windowTimeout = Duration.ofSeconds(60);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(window0Size, windowTimeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(window1Size, windowTimeout);
        final Flux<Integer> window0Flux = r0.getWindowFlux();
        final Flux<Integer> window1Flux = r1.getWindowFlux();

        StepVerifier.create(window0Flux)
            .then(() -> upstream.next(1))
            .then(() -> upstream.next(2))
            .then(() -> upstream.next(3))
            .then(() -> upstream.next(4))
            .then(() -> upstream.next(5))
            .expectNext(1, 2, 3, 4, 5)
            .verifyComplete();

        // Ensure that drain-loop will move to the next window after previous window closure.
        StepVerifier.create(window1Flux)
            .then(() -> upstream.next(6))
            .then(() -> upstream.next(7))
            .then(() -> upstream.next(8))
            .expectNext(6, 7, 8)
            .verifyComplete();

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertEquals(0, work0.getPending());
        Assertions.assertFalse(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertEquals(0, work1.getPending());
        Assertions.assertFalse(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldCancelWorkOnEmptyWindowTruncation() {
        final int windowSize = 50;
        final int cancelAfter = 0;
        final Duration windowTimeout = Duration.ofSeconds(20);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        // Starting from reactor-core-3.6.x, if we use take(n:0) to take none, downstream will be 'completed' without
        // 'canceling' upstream. To make it cancel upstream, use take(n:0, limitRequest:false).
        // https://github.com/reactor/reactor-core/issues/3839
        final Flux<Integer> windowFlux = r.getWindowFlux().take(cancelAfter, false);

        StepVerifier.create(windowFlux).verifyComplete();

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertTrue(work.isCanceled());
        Assertions.assertFalse(work.hasTimedOut());
    }

    @Test
    public void shouldCancelWorkOnStreamingWindowTruncation() {
        final int windowSize = 50;
        final int cancelAfter = 2;
        final Duration windowTimeout = Duration.ofSeconds(20);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<Integer> windowFlux = r.getWindowFlux().take(cancelAfter);

        StepVerifier.create(windowFlux)
            .then(() -> upstream.next(1))
            .then(() -> upstream.next(2))
            .then(() -> upstream.next(3))
            .expectNext(1, 2) // windowFlux cancels here
            .verifyComplete();

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertTrue(work.isCanceled());
        Assertions.assertFalse(work.hasTimedOut());
    }

    @Test
    public void shouldInvokeWindowDecorator() {
        final int windowSize = 2;
        final Duration windowTimeout = Duration.ofSeconds(20);
        final Decorator decorator = new Decorator();
        final Upstream<String> upstream = new Upstream<>();

        final WindowedSubscriberOptions<String> options = new WindowedSubscriberOptions<>();
        final WindowedSubscriber<String> subscriber = createSubscriber(options.setWindowDecorator(decorator));
        upstream.subscribe(subscriber);

        EnqueueResult<String> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final Flux<String> windowFlux = r.getWindowFlux();

        StepVerifier.create(windowFlux)
            .then(() -> upstream.next("A"))
            .then(() -> upstream.next("B"))
            .expectNext(Decorator.PREFIX + "A", Decorator.PREFIX + "B")
            .verifyComplete();

        final WindowedSubscriber.WindowWork<String> work = r.getInnerWork();
        Assertions.assertFalse(work.isCanceled());
        Assertions.assertFalse(work.hasTimedOut());
    }

    static void assertUpstreamErrorMessage(String message) {
        Assertions.assertTrue((TERMINATED_MESSAGE + " (Reason: upstream-error)").equals(message));
    }

    static void assertUpstreamCompletedMessage(String message) {
        Assertions.assertTrue((TERMINATED_MESSAGE + " (Reason: upstream-completion)").equals(message));
    }

    static void assertDownstreamCanceledMessage(String message) {
        Assertions.assertTrue((TERMINATED_MESSAGE + " (Reason: downstream-cancel)").equals(message));
    }

    static WindowedSubscriber<Integer> createSubscriber() {
        return new WindowedSubscriber<>(EMPTY_LOGGING_CONTEXT, TERMINATED_MESSAGE, new WindowedSubscriberOptions<>());
    }

    static <T> WindowedSubscriber<T> createSubscriber(WindowedSubscriberOptions<T> options) {
        return new WindowedSubscriber<>(EMPTY_LOGGING_CONTEXT, TERMINATED_MESSAGE, options);
    }

    static final class Upstream<T> {
        private final AtomicLong requested = new AtomicLong(0);
        private final Sinks.Many<T> sink = Sinks.many().multicast().onBackpressureBuffer();

        Upstream() {
        }

        Disposable subscribe(WindowedSubscriber<T> subscriber) {
            return sink.asFlux().doOnRequest(r -> {
                requested.addAndGet(r);
            }).subscribeWith(subscriber);
        }

        long getRequested() {
            return requested.get();
        }

        void next(T value) {
            if (requested.getAndDecrement() <= 0) {
                throw new IllegalStateException("No demand pending from WindowMessagesSubscriber.");
            }
            sink.emitNext(value, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void error(Throwable error) {
            sink.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void complete() {
            sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }
    }

    static final class Decorator implements Function<Flux<String>, Flux<String>> {
        static final String PREFIX = "decorated-";

        @Override
        public Flux<String> apply(Flux<String> toDecorate) {
            return toDecorate.map(v -> (PREFIX + v));
        }
    }
}
