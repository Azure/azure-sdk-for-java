// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.IterableStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.azure.core.amqp.implementation.WindowedSubscriber.EnqueueResult;
import com.azure.core.amqp.implementation.WindowedSubscriber.WindowedSubscriberOptions;

import static com.azure.core.amqp.implementation.WindowedSubscriberFluxWindowTest.assertDownstreamCanceledMessage;
import static com.azure.core.amqp.implementation.WindowedSubscriberFluxWindowTest.assertUpstreamCompletedMessage;
import static com.azure.core.amqp.implementation.WindowedSubscriberFluxWindowTest.assertUpstreamErrorMessage;
import static com.azure.core.amqp.implementation.WindowedSubscriberFluxWindowTest.createSubscriber;
import static com.azure.core.amqp.implementation.WindowedSubscriberFluxWindowTest.Decorator;
import static com.azure.core.amqp.implementation.WindowedSubscriberFluxWindowTest.Upstream;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class WindowedSubscriberIterableWindowTest {

    @Test
    public void shouldValidateEnqueueParameters() {
        final Duration timeout = Duration.ofSeconds(1);
        WindowedSubscriber<Integer> subscriber = createSubscriber();

        assertThrows(IllegalArgumentException.class, () -> subscriber.enqueueRequestImpl(0, timeout));
        assertThrows(IllegalArgumentException.class, () -> subscriber.enqueueRequestImpl(-1, timeout));
        assertThrows(NullPointerException.class, () -> subscriber.enqueueRequestImpl(1, null));
    }

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
        final IterableStream<Integer> windowIterable = r.getWindowIterable();

        final RuntimeException e = Assertions.assertThrows(RuntimeException.class, () -> {
            windowIterable.stream().collect(Collectors.toList());
        });
        final String message = e.getMessage();
        Assertions.assertNotNull(message);
        assertUpstreamErrorMessage(message);
        Assertions.assertEquals(upstreamError, e.getCause());
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
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);

        final ArrayList<Integer> list0 = new ArrayList<>();
        final Function<Integer, Integer> mapper0 = v -> {
            list0.add(v);
            if (v == 2) {
                upstream.error(upstreamError);
            }
            return v;
        };
        final RuntimeException e0 = Assertions.assertThrows(RuntimeException.class, () -> {
            window0Iterable.stream().map(mapper0).collect(Collectors.toList());
        });
        final String message0 = e0.getMessage();
        Assertions.assertNotNull(message0);
        assertUpstreamErrorMessage(message0);
        Assertions.assertEquals(upstreamError, e0.getCause());
        Assertions.assertEquals(Arrays.asList(1, 2), list0);

        final ArrayList<Integer> list1 = new ArrayList<>();
        final Function<Integer, Integer> mapper1 = v -> {
            list1.add(v);
            return v;
        };
        final RuntimeException e1 = Assertions.assertThrows(RuntimeException.class, () -> {
            window1Iterable.stream().map(mapper1).collect(Collectors.toList());
        });
        final String message1 = e1.getMessage();
        Assertions.assertNotNull(message1);
        assertUpstreamErrorMessage(message1);
        Assertions.assertEquals(upstreamError, e1.getCause());
        Assertions.assertEquals(0, list1.size());

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
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);

        final ArrayList<Integer> list0 = new ArrayList<>();
        final Function<Integer, Integer> mapper0 = v -> {
            list0.add(v);
            if (v == 2) {
                upstream.error(upstreamError);
            }
            return v;
        };
        // Clean close since window0Iterable was in 'streaming state' (aka 'streaming window').
        final List<Integer> list00 = window0Iterable.stream().map(mapper0).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 2), list00);

        final ArrayList<Integer> list1 = new ArrayList<>();
        final Function<Integer, Integer> mapper1 = v -> {
            list1.add(v);
            return v;
        };
        // Any work (window1Iterable) enqueued after the closure of 'streaming window' will be closed with error.
        final RuntimeException e1 = Assertions.assertThrows(RuntimeException.class, () -> {
            window1Iterable.stream().map(mapper1).collect(Collectors.toList());
        });
        final String message1 = e1.getMessage();
        Assertions.assertNotNull(message1);
        assertUpstreamErrorMessage(message1);
        Assertions.assertEquals(upstreamError, e1.getCause());
        Assertions.assertEquals(0, list1.size());

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
        final IterableStream<Integer> windowIterable = r.getWindowIterable();
        final RuntimeException e = Assertions.assertThrows(RuntimeException.class, () -> {
            windowIterable.stream().collect(Collectors.toList());
        });
        final String message = e.getMessage();
        Assertions.assertNotNull(message);
        assertUpstreamCompletedMessage(message);
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
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);

        final ArrayList<Integer> list0 = new ArrayList<>();
        final Function<Integer, Integer> mapper0 = v -> {
            list0.add(v);
            if (v == 2) {
                upstream.complete();
            }
            return v;
        };
        final RuntimeException e0 = Assertions.assertThrows(RuntimeException.class, () -> {
            window0Iterable.stream().map(mapper0).collect(Collectors.toList());
        });
        final String message0 = e0.getMessage();
        Assertions.assertNotNull(message0);
        assertUpstreamCompletedMessage(message0);
        Assertions.assertEquals(Arrays.asList(1, 2), list0);

        final ArrayList<Integer> list1 = new ArrayList<>();
        final Function<Integer, Integer> mapper1 = v -> {
            list1.add(v);
            return v;
        };
        final RuntimeException e1 = Assertions.assertThrows(RuntimeException.class, () -> {
            window1Iterable.stream().map(mapper1).collect(Collectors.toList());
        });
        final String message1 = e1.getMessage();
        Assertions.assertNotNull(message1);
        assertUpstreamCompletedMessage(message1);
        Assertions.assertEquals(0, list1.size());

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
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);

        final ArrayList<Integer> list0 = new ArrayList<>();
        final Function<Integer, Integer> mapper0 = v -> {
            list0.add(v);
            if (v == 2) {
                upstream.complete();
            }
            return v;
        };
        // Clean close since window0Iterable was in 'streaming state' (aka 'streaming window').
        final List<Integer> list00 = window0Iterable.stream().map(mapper0).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 2), list00);

        final ArrayList<Integer> list1 = new ArrayList<>();
        final Function<Integer, Integer> mapper1 = v -> {
            list1.add(v);
            return v;
        };
        // Any work (window1Iterable) enqueued after the closure of 'streaming window' will be closed with error.
        final RuntimeException e1 = Assertions.assertThrows(RuntimeException.class, () -> {
            window1Iterable.stream().map(mapper1).collect(Collectors.toList());
        });
        final String message1 = e1.getMessage();
        Assertions.assertNotNull(message1);
        assertUpstreamCompletedMessage(message1);
        Assertions.assertEquals(0, list1.size());

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
        final IterableStream<Integer> windowIterable = r.getWindowIterable();
        final RuntimeException e = Assertions.assertThrows(RuntimeException.class, () -> {
            windowIterable.stream().collect(Collectors.toList());
        });
        final String message = e.getMessage();
        Assertions.assertNotNull(message);
        assertDownstreamCanceledMessage(message);

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
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);

        final ArrayList<Integer> list0 = new ArrayList<>();
        final Function<Integer, Integer> mapper0 = v -> {
            list0.add(v);
            if (v == 2) {
                disposable.dispose();
            }
            return v;
        };
        final RuntimeException e0 = Assertions.assertThrows(RuntimeException.class, () -> {
            window0Iterable.stream().map(mapper0).collect(Collectors.toList());
        });
        final String message0 = e0.getMessage();
        Assertions.assertNotNull(message0);
        assertDownstreamCanceledMessage(message0);
        Assertions.assertEquals(Arrays.asList(1, 2), list0);

        final ArrayList<Integer> list1 = new ArrayList<>();
        final Function<Integer, Integer> mapper1 = v -> {
            list1.add(v);
            return v;
        };
        final RuntimeException e1 = Assertions.assertThrows(RuntimeException.class, () -> {
            window1Iterable.stream().map(mapper1).collect(Collectors.toList());
        });
        final String message1 = e1.getMessage();
        Assertions.assertNotNull(message1);
        assertDownstreamCanceledMessage(message1);
        Assertions.assertEquals(0, list1.size());

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
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);

        final ArrayList<Integer> list0 = new ArrayList<>();
        final Function<Integer, Integer> mapper0 = v -> {
            list0.add(v);
            if (v == 2) {
                disposable.dispose();
            }
            return v;
        };
        // Clean close since window0Iterable was in 'streaming state' (aka 'streaming window').
        final List<Integer> list00 = window0Iterable.stream().map(mapper0).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 2), list00);

        final ArrayList<Integer> list1 = new ArrayList<>();
        final Function<Integer, Integer> mapper1 = v -> {
            list1.add(v);
            return v;
        };
        // Any work (window1Iterable) enqueued after the closure of 'streaming window' will be closed with error.
        final RuntimeException e1 = Assertions.assertThrows(RuntimeException.class, () -> {
            window1Iterable.stream().map(mapper1).collect(Collectors.toList());
        });
        final String message1 = e1.getMessage();
        Assertions.assertNotNull(message1);
        assertDownstreamCanceledMessage(message1);
        Assertions.assertEquals(0, list1.size());

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
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);
        upstream.next(3);
        upstream.next(4);
        upstream.next(5);
        //
        upstream.next(6);
        upstream.next(7);
        upstream.next(8);

        final List<Integer> list0 = window0Iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4, 5), list0);

        // Ensure that drain-loop will move to the next window after previous window closure.
        final List<Integer> list1 = window1Iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(6, 7, 8), list1);

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
        final Duration windowTimeout = Duration.ofSeconds(20);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final IterableStream<Integer> windowIterable = r.getWindowIterable();

        final ArrayList<Integer> list0 = new ArrayList<>();

        // Use limit(0) to truncate as empty window.
        try (Stream<Integer> stream = windowIterable.stream().limit(0)) {
            // It is required to close the stream if for any reason the IterableStream gets truncated, so use of 'try'.
            // Don't truncate the stream in production code as it may lead to incrementing delivery count or losing
            // messages already loaded into the window buffer.
            stream.forEach(v -> {
                list0.add(v);
            });
        }
        Assertions.assertEquals(0, list0.size());

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertTrue(work.isCanceled());
        Assertions.assertFalse(work.hasTimedOut());
    }

    @Test
    public void shouldCancelWorkOnStreamingWindowTruncation() {
        final int windowSize = 50;
        final int cancelAfter = 2; // a value less than windowSize.
        final Duration windowTimeout = Duration.ofSeconds(20);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final IterableStream<Integer> windowIterable = r.getWindowIterable();

        upstream.next(1);
        upstream.next(2);
        upstream.next(3);

        final ArrayList<Integer> list0 = new ArrayList<>();

        // Use limit(int) to truncate window.
        try (Stream<Integer> stream = windowIterable.stream().limit(cancelAfter)) {
            // It is required to close the stream if for any reason the IterableStream gets truncated, so use of 'try'.
            // Don't truncate the stream in production code as it may lead to incrementing delivery count or losing
            // messages already loaded into the window buffer.
            stream.forEach(v -> {
                list0.add(v);
            });
        }
        Assertions.assertEquals(Arrays.asList(1, 2), list0);

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertTrue(work.isCanceled());
        Assertions.assertFalse(work.hasTimedOut());
    }

    @Test
    public void shouldContinueToNextWindowWhenStreamingWindowCancels() {
        final int windowSize = 50;
        final int cancelAfter = 2; // a value less than windowSize.
        final Duration window0Timeout = Duration.ofSeconds(20);
        final Duration window1Timeout = Duration.ofSeconds(1);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, window0Timeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, window1Timeout);
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);
        upstream.next(3);

        final ArrayList<Integer> list0 = new ArrayList<>();

        // Use limit(int) to cancel window.
        try (Stream<Integer> stream = window0Iterable.stream().limit(cancelAfter)) {
            // It is required to close the stream if for any reason the IterableStream gets truncated, so use of 'try'.
            // Don't truncate the stream in production code as it may lead to incrementing delivery count or losing
            // messages already loaded into the window buffer.
            stream.forEach(v -> {
                list0.add(v);
            });
        }
        Assertions.assertEquals(Arrays.asList(1, 2), list0);

        // Ensure that drain-loop will move to the next window after previous window closure.
        window1Iterable.stream().collect(Collectors.toList());

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertNotEquals(windowSize, work0.getPending());
        Assertions.assertTrue(work0.isCanceled());
        Assertions.assertFalse(work0.hasTimedOut());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertTrue(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldCloseEmptyWindowOnTimeout() {
        final int windowSize = 10;
        final Duration windowTimeout = Duration.ofSeconds(2); // use small timeout (No VTScheduler)
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final IterableStream<Integer> windowIterable = r.getWindowIterable();

        final List<Integer> list0 = windowIterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(0, list0.size());

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertTrue(work.hasTimedOut());
        Assertions.assertFalse(work.isCanceled());
    }

    @Test
    public void shouldCloseStreamingWindowOnTimeout() {
        final int windowSize = 10;
        final Duration windowTimeout = Duration.ofSeconds(2); // use small timeout (No VTScheduler)
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final IterableStream<Integer> windowIterable = r.getWindowIterable();

        upstream.next(1);
        upstream.next(2);

        final List<Integer> list0 = windowIterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 2), list0);

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertTrue(work.hasTimedOut());
        Assertions.assertFalse(work.isCanceled());
    }

    @Test
    public void shouldContinueToNextWindowOnEmptyWindowTimeout() {
        final int windowSize = 10;
        // use small timeouts (No VTScheduler)
        final Duration window0Timeout = Duration.ofMillis(500);
        final Duration window1Timeout = Duration.ofSeconds(2);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, window0Timeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, window1Timeout);
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        // When time out triggers, it will close the window0Iterable stream, which will end the blocking collect() call
        // by returning "empty" list since "no events were received" within the timeout.
        final List<Integer> list0 = window0Iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(0, list0.size());

        // Ensure that drain-loop will move to the next window after previous window closure.
        upstream.next(1);
        upstream.next(2);
        final List<Integer> list1 = window1Iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 2), list1);

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertEquals(windowSize, work0.getPending());
        Assertions.assertTrue(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertNotEquals(windowSize, work1.getPending());
        Assertions.assertTrue(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldContinueToNextWindowWhenStreamingWindowTimeout() {
        final int windowSize = 50;
        final Duration window0Timeout = Duration.ofSeconds(1);
        final Duration window1Timeout = Duration.ofSeconds(1);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, window0Timeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, window1Timeout);
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);

        // When time out triggers, it will close the window0Iterable stream, which will end the blocking collect() call
        // and return the list, list0, with events received so far (which is less than demanded).
        final List<Integer> list0 = window0Iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 2), list0);

        // Ensure that drain-loop will move to the next window after previous window closure.
        upstream.next(3);
        upstream.next(4);
        final List<Integer> list1 = window1Iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(3, 4), list1);

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertNotEquals(windowSize, work0.getPending());
        Assertions.assertTrue(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertNotEquals(windowSize, work1.getPending());
        Assertions.assertTrue(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldContinueToNextWindowWhenStreamingWindowThrows() {
        final int windowSize = 50;
        final Duration window0Timeout = Duration.ofSeconds(20);
        final Duration window1Timeout = Duration.ofSeconds(1);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(windowSize, window0Timeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(windowSize, window1Timeout);
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        upstream.next(1);
        upstream.next(2);
        upstream.next(3);

        final ArrayList<Integer> list0 = new ArrayList<>();

        final RuntimeException e = Assertions.assertThrows(RuntimeException.class, () -> {
            try (Stream<Integer> stream = window0Iterable.stream()) {
                // It is required to close the stream if for any reason the IterableStream processing stops in the middle
                // (e.g., throwing exception), so use of 'try'.
                // Don't truncate the stream in production code as it may lead to incrementing delivery count or losing
                // messages already loaded into the window buffer.
                stream.forEach(v -> {
                    if (v == 3) {
                        throw new RuntimeException("application-error");
                    }
                    list0.add(v);
                });
            }
        });
        Assertions.assertTrue(e.getMessage().equals("application-error"));

        Assertions.assertEquals(Arrays.asList(1, 2), list0);
        final List<Integer> list1 = window1Iterable.stream().collect(Collectors.toList());

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertTrue(work0.isCanceled());
        Assertions.assertFalse(work0.hasTimedOut());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertTrue(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
    }

    @Test
    public void shouldRequestWindowDemand() {
        final int windowSize = 50;
        final Duration windowTimeout = Duration.ofMillis(500);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final IterableStream<Integer> windowIterable = r.getWindowIterable();
        final List<Integer> list = windowIterable.stream().collect(Collectors.toList());

        Assertions.assertEquals(windowSize, upstream.getRequested());

        final WindowedSubscriber.WindowWork<Integer> work = r.getInnerWork();
        Assertions.assertTrue(work.hasTimedOut());
        Assertions.assertFalse(work.isCanceled());
    }

    @Test
    public void shouldAccountPendingRequestWhenServingNextWindowDemand() {
        final int window0Size = 10;
        final int window1Size = 25;
        final Duration windowTimeout = Duration.ofMillis(500);
        final Upstream<Integer> upstream = new Upstream<>();

        final WindowedSubscriber<Integer> subscriber = createSubscriber();
        upstream.subscribe(subscriber);

        final EnqueueResult<Integer> r0 = subscriber.enqueueRequestImpl(window0Size, windowTimeout);
        final EnqueueResult<Integer> r1 = subscriber.enqueueRequestImpl(window1Size, windowTimeout);
        final IterableStream<Integer> window0Iterable = r0.getWindowIterable();
        final IterableStream<Integer> window1Iterable = r1.getWindowIterable();

        window0Iterable.stream().collect(Collectors.toList());
        window1Iterable.stream().collect(Collectors.toList());

        Assertions.assertEquals(window0Size + (window1Size - window0Size), upstream.getRequested());

        final WindowedSubscriber.WindowWork<Integer> work0 = r0.getInnerWork();
        Assertions.assertTrue(work0.hasTimedOut());
        Assertions.assertFalse(work0.isCanceled());

        final WindowedSubscriber.WindowWork<Integer> work1 = r1.getInnerWork();
        Assertions.assertTrue(work1.hasTimedOut());
        Assertions.assertFalse(work1.isCanceled());
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

        final EnqueueResult<String> r = subscriber.enqueueRequestImpl(windowSize, windowTimeout);
        final IterableStream<String> windowIterable = r.getWindowIterable();

        upstream.next("A");
        upstream.next("B");
        final List<String> list0 = windowIterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(Decorator.PREFIX + "A", Decorator.PREFIX + "B"), list0);

        final WindowedSubscriber.WindowWork<String> work = r.getInnerWork();
        Assertions.assertFalse(work.hasTimedOut());
        Assertions.assertFalse(work.isCanceled());
    }
}
