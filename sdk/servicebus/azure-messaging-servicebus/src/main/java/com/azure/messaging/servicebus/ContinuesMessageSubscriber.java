// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Operators;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.context.Context;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Processor that listens to upstream messages, pushes them downstream then completes it if necessary.
 */
class ContinuesMessageSubscriber extends FluxProcessor<ServiceBusReceivedMessageContext,
    ServiceBusReceivedMessageContext> implements Subscription {

    private final ClientLogger logger = new ClientLogger(ContinuesMessageSubscriber.class);

    private volatile boolean isDone;
    private volatile FluxSink<ServiceBusReceivedMessageContext> downstream;

    private volatile boolean isCancelled;
    private final Deque<ServiceBusReceivedMessageContext> messageQueue = new ConcurrentLinkedDeque<>();
    private final int maximumMessageCount;

    ContinuesMessageSubscriber(int maximumMessageCount, FluxSink<ServiceBusReceivedMessageContext> emitter) {
        super();
        this.maximumMessageCount = maximumMessageCount;
        this.downstream = emitter;
    }

    volatile Subscription upstream;
    private static final AtomicReferenceFieldUpdater<ContinuesMessageSubscriber, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(ContinuesMessageSubscriber.class, Subscription.class,
            "upstream");

    volatile int once;
    static final AtomicIntegerFieldUpdater<ContinuesMessageSubscriber> ONCE =
        AtomicIntegerFieldUpdater.newUpdater(ContinuesMessageSubscriber.class, "once");

    volatile int wip;
    static final AtomicIntegerFieldUpdater<ContinuesMessageSubscriber> WIP =
        AtomicIntegerFieldUpdater.newUpdater(ContinuesMessageSubscriber.class, "wip");

    volatile long requested;
    static final AtomicLongFieldUpdater<ContinuesMessageSubscriber> REQUESTED =
        AtomicLongFieldUpdater.newUpdater(ContinuesMessageSubscriber.class, "requested");

    volatile Throwable error;
    static final AtomicReferenceFieldUpdater<ContinuesMessageSubscriber, Throwable> ERROR =
        AtomicReferenceFieldUpdater.newUpdater(ContinuesMessageSubscriber.class, Throwable.class, "error");

    @Override
    public void onSubscribe(Subscription subscription) {
        Objects.requireNonNull(subscription, "'subscription' cannot be null.");

        if (Operators.setOnce(UPSTREAM, this, subscription)) {

            Operators.addCap(REQUESTED, this, maximumMessageCount);

            subscription.request(maximumMessageCount);
        } else {
            final Throwable error = logger.logExceptionAsError(new IllegalStateException(
                "Processor cannot be subscribed to with multiple upstreams."));

            onError(Operators.onOperatorError(subscription, error, Context.empty()));
        }
    }

    @Override
    public boolean isTerminated() {
        return isDone || isCancelled;
    }

    @Override
    public void onNext(ServiceBusReceivedMessageContext message) {
        if (isTerminated()) {
            final Context context = downstream == null ? currentContext() : downstream.currentContext();
            Operators.onNextDropped(message, context);

            return;
        }

        messageQueue.add(message);
        drain();
    }

    /**
     * Invoked when an error occurs upstream.
     *
     * @param throwable Error that occurred upstream.
     */
    @Override
    public void onError(Throwable throwable) {
        if (isDone || isCancelled) {
            logger.error("Exception occurred from upstream when this is already terminated.", throwable);
            Operators.onErrorDropped(throwable, currentContext());
            return;
        }

        if (Exceptions.addThrowable(ERROR, this, throwable)) {
            isDone = true;
        } else {
            Operators.onErrorDropped(throwable, currentContext());
        }

        drain();
    }

    /**
     * Invoked when upstream has finished emitting items.
     */
    @Override
    public void onComplete() {
        if (isDone) {
            return;
        }

        isDone = true;
        drain();
    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessageContext> downstream) {
        Objects.requireNonNull(downstream, "'downstream' cannot be null.");
        if (once == 0 && ONCE.compareAndSet(this, 0, 1)) {
            if (isCancelled) {
                this.downstream = null;
            } else {
                drain();
            }
        } else {
            Operators.error(downstream,
                new IllegalStateException("ContinuesMessageSubscriber can only have one subscriber."));
        }
    }

    void request(long request, FluxSink<ServiceBusReceivedMessageContext> emitter) {
        this.downstream = emitter;
        request(request);
    }
    @Override
    public void request(long request) {
        logger.info("Back-pressure request: {}", request);
        if (Operators.validate(request)) {
            Operators.addCap(REQUESTED, this, request);

            if (upstream != null) {
                upstream.request(request);
            }

            drain();
        }
    }

    @Override
    public void cancel() {
        if (isCancelled) {
            return;
        }

        logger.info("Cancelling subscription.");
        isCancelled = true;
        drain();
    }

    private void drain() {
        // If someone is already in this loop, then we are already clearing the queue.
        if (!WIP.compareAndSet(this, 0, 1)) {
            return;
        }

        try {
            drainQueue();
        } finally {
            if (WIP.decrementAndGet(this) != 0) {
                logger.warning("There is another worker in drainLoop. But there should only be 1 worker.");
            }
        }
    }

    private void drainQueue() {
        if (downstream == null) {
            return;
        }

        while (true) {
            if (messageQueue.isEmpty()) {
                break;
            }

            long amountRequested = REQUESTED.get(this);
            long emitted = drainRequested(amountRequested);

            // We emitted the correct number that was requested.
            // Nothing more requested since.
            if (REQUESTED.addAndGet(this, -emitted) == 0) {
                this.downstream.complete();
                break;
            }

            if (isDone) {
                break;
            }

        }

        if (isDone) {
            if (error != null) {
                downstream.error(error);
            } else if (messageQueue.peekLast() == null) {
                downstream.complete();
            } else {
                Operators.onDiscardQueueWithClear(messageQueue, downstream.currentContext(), null);
            }

            downstream = null;
        }
    }

    /**
     * Drains the queue of the requested amount and returns the number taken.
     *
     * @param numberRequested Number of items requested.
     *
     * @return The number of items emitted.
     */
    private long drainRequested(long numberRequested) {
        long numberEmitted = 0L;

        if (numberRequested == 0L) {
            return numberEmitted;
        }

        for (; numberEmitted < numberRequested; numberEmitted++) {
            if (isDone) {
                return numberEmitted;
            }

            final ServiceBusReceivedMessageContext message = messageQueue.poll();
            if (message == null) {
                break;
            }

            if (isCancelled) {
                Operators.onDiscard(message, downstream.currentContext());
                Operators.onDiscardQueueWithClear(messageQueue, downstream.currentContext(), null);
                break;
            }

            try {
                next(message);
            } catch (Exception e) {
                setInternalError(e);
                break;
            }
        }

        return numberEmitted;
    }

    private void next(ServiceBusReceivedMessageContext message) {

        final AtomicBoolean hasError = new AtomicBoolean();

        try {
            downstream.next(message);
        } catch (Exception e) {
            hasError.set(true);
            logger.error("Exception occurred while handling downstream onNext operation.", e);

            setInternalError(e);
        }
    }

    private void setInternalError(Throwable error) {
        if (Exceptions.addThrowable(ERROR, this, error)) {
            isDone = true;
        } else {
            Operators.onErrorDropped(error, downstream.currentContext());
        }
    }

    @Override
    public void dispose() {
        if (isDone) {
            return;
        }

        logger.info("Disposing subscription.");
        isDone = true;
        drain();
    }

    @Override
    public boolean isDisposed() {
        return isDone || isCancelled;
    }

}
