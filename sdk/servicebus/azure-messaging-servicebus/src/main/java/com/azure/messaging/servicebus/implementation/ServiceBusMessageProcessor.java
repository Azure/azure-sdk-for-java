// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

/**
 * Processor that listens to upstream messages, pushes them downstream then completes it if necessary.
 */
class ServiceBusMessageProcessor extends FluxProcessor<ServiceBusReceivedMessage, ServiceBusReceivedMessage>
    implements Subscription {
    private final ClientLogger logger = new ClientLogger(ServiceBusMessageProcessor.class);
    private final boolean isAutoComplete;
    private final Function<ServiceBusReceivedMessage, Mono<Void>> completeFunction;
    private final Deque<ServiceBusReceivedMessage> messageQueue = new ConcurrentLinkedDeque<>();

    ServiceBusMessageProcessor(boolean isAutoComplete,
        Function<ServiceBusReceivedMessage, Mono<Void>> completeFunction) {
        super();
        this.isAutoComplete = isAutoComplete;
        this.completeFunction = Objects.requireNonNull(completeFunction, "'completeFunction' cannot be null.");
    }

    private volatile boolean isDone;
    private volatile CoreSubscriber<? super ServiceBusReceivedMessage> downstream;
    private volatile boolean isCancelled;

    volatile Subscription upstream;
    private static final AtomicReferenceFieldUpdater<ServiceBusMessageProcessor, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, Subscription.class,
            "upstream");

    volatile int once;
    static final AtomicIntegerFieldUpdater<ServiceBusMessageProcessor> ONCE =
        AtomicIntegerFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, "once");

    volatile int wip;
    static final AtomicIntegerFieldUpdater<ServiceBusMessageProcessor> WIP =
        AtomicIntegerFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, "wip");

    volatile long requested;
    static final AtomicLongFieldUpdater<ServiceBusMessageProcessor> REQUESTED =
        AtomicLongFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, "requested");

    private volatile Throwable error;

    /**
     * Invoked when this subscribes to an upstream publisher.
     *
     * @param subscription Subscription for the upstream publisher.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        Objects.requireNonNull(subscription, "'subscription' cannot be null.");

        if (Operators.setOnce(UPSTREAM, this, subscription)) {
            subscription.request(1);
        } else {
            logger.warning("This processor cannot be subscribed to with multiple upstreams.");
        }
    }

    @Override
    public void onNext(ServiceBusReceivedMessage message) {
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

        error = throwable;
        isDone = true;

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
        isCancelled = true;
        drain();
    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> downstream) {
        Objects.requireNonNull(downstream, "'downstream' cannot be null.");
        if (once == 0 && ONCE.compareAndSet(this, 0, 1)) {
            downstream.onSubscribe(this);
            this.downstream = downstream;
            if (isCancelled) {
                this.downstream = null;
            } else {
                drain();
            }
        } else {
            Operators.error(downstream,
                new IllegalStateException("ServiceBusMessageSubscriber can only have one subscriber."));
        }
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

        final ServiceBusReceivedMessage lastMessage = messageQueue.peekLast();
        if (lastMessage == null) {
            if (isDone) {
                downstream.onComplete();
            }
            return;
        }

        ServiceBusReceivedMessage message = messageQueue.poll();
        while (message != lastMessage) {
            if (message == null) {
                logger.warning("The last message is not null, but the head node is null. lastMessage: {}", lastMessage);
                message = messageQueue.poll();
                continue;
            }

            if (isCancelled) {
                Operators.onDiscard(message, downstream.currentContext());
                Operators.onDiscardQueueWithClear(messageQueue, downstream.currentContext(), null);
                return;
            }

            next(message);

            message = messageQueue.poll();
        }

        // Emit the message which is equal to lastMessage.
        next(message);

        if (isDone) {
            if (error != null) {
                downstream.onError(error);
            } else if (messageQueue.peekLast() == null) {
                downstream.onComplete();
            }
        }
    }

    private void next(ServiceBusReceivedMessage message) {
        try {
            downstream.onNext(message);
            if (isAutoComplete) {
                completeFunction.apply(message).block();
            }
        } catch (Exception e) {
            logger.error("Exception occurred while handling downstream onNext operation.", e);
            Operators.onOperatorError(upstream, e, message, downstream.currentContext());
        }
    }
}
