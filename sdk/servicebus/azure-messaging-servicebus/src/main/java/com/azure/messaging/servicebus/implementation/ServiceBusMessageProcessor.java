// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AmqpRetryOptions retryOptions;
    private final Function<ServiceBusReceivedMessage, Mono<Void>> completeFunction;
    private final Function<ServiceBusReceivedMessage, Mono<Void>> onAbandon;
    private final Deque<ServiceBusReceivedMessage> messageQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, PendingComplete> pendingCompletes = new HashMap<>();

    ServiceBusMessageProcessor(boolean isAutoComplete, AmqpRetryOptions retryOptions,
        Function<ServiceBusReceivedMessage, Mono<Void>> completeFunction,
        Function<ServiceBusReceivedMessage, Mono<Void>> onAbandon) {
        super();
        this.isAutoComplete = isAutoComplete;
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.completeFunction = Objects.requireNonNull(completeFunction, "'completeFunction' cannot be null.");
        this.onAbandon = Objects.requireNonNull(onAbandon, "'onAbandon' cannot be null.");;
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
        final UUID lockToken = message.getLockToken();
        final boolean isCompleteMessage = isAutoComplete && lockToken != null
            && !MessageUtils.ZERO_LOCK_TOKEN.equals(lockToken);

        logger.info("sequenceNumber[{}]. lock[{}]. Auto-complete message? [{}]",
            message.getSequenceNumber(), lockToken, isCompleteMessage);

        final PendingComplete pendingComplete = new PendingComplete(message);
        if (isCompleteMessage) {
            pendingCompletes.put(lockToken, pendingComplete);
        }

        try {
            downstream.onNext(message);
        } catch (Exception e) {
            logger.error("Exception occurred while handling downstream onNext operation.", e);

            final PendingComplete pending = pendingCompletes.get(lockToken);
            if (isCompleteMessage && pending != null) {
                logger.info("Abandoning message lock: {}", lockToken);
                onAbandon.apply(message)
                    .onErrorStop()
                    .doOnError(error -> {
                        logger.warning("Could not abandon message with lock: {}", lockToken, error);
                        pending.error(error);
                    })
                    .doOnSuccess(unused -> pending.complete())
                    .doFinally(signal -> {
                        logger.info("lock[{}]. Abandon status: [{}]", lockToken, signal);
                        pendingCompletes.remove(lockToken);
                    })
                    .block();
            }

            downstream.onError(Operators.onOperatorError(upstream, e, message, downstream.currentContext()));
        }

        try {
            // check that the pending operation is in the queue and not running yet.
            final PendingComplete pending = pendingCompletes.get(lockToken);
            if (isCompleteMessage && pending != null && !pending.isRunningGetAndSet()) {
                final ServiceBusReceivedMessage completedMessage = pending.getMessage();

                logger.info("sequenceNumber[{}]. lock[{}]. Completing message.",
                    completedMessage.getSequenceNumber(), completedMessage.getLockToken());

                completeFunction.apply(completedMessage)
                    .onErrorStop()
                    .doOnError(error -> {
                        logger.warning("Could not complete message with lock: {}",
                            completedMessage.getLockToken(), error);

                        pending.error(error);
                    })
                    .doOnSuccess(ignored -> pending.complete())
                    .doFinally(signal -> {
                        logger.info("lock[{}]. Complete status: [{}]", completedMessage.getLockToken(), signal);

                        pendingCompletes.remove(lockToken);
                    })
                    .block(retryOptions.getTryTimeout());
            }
        } catch (Exception e) {
            logger.error("Exception occurred while auto-completing message. Sequence: {}. Lock token: {}",
                message.getSequenceNumber(), message.getLockToken(), e);
            downstream.onError(Operators.onOperatorError(upstream, e, message, downstream.currentContext()));
        }
    }

    private static final class PendingComplete {
        private final ServiceBusReceivedMessage message;
        private final AtomicBoolean isRunning = new AtomicBoolean();
        private final Mono<Void> onComplete;
        private MonoSink<Void> sink;

        private PendingComplete(ServiceBusReceivedMessage message) {
            this.message = message;
            this.onComplete = Mono.create(sink -> {
                this.sink = sink;
            });
            onComplete.subscribe();
        }

        private Mono<Void> getOnComplete() {
            return onComplete;
        }

        private boolean isRunningGetAndSet() {
            return isRunning.getAndSet(true);
        }

        private void error(Throwable throwable) {
            if (sink != null) {
                sink.error(throwable);
            }
        }

        private void complete() {
            if (sink != null) {
                sink.success();
            }
        }

        private ServiceBusReceivedMessage getMessage() {
            return message;
        }
    }
}
