// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.MessageLockToken;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Objects;
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
    private final AmqpErrorContext errorContext;
    private final Function<MessageLockToken, Mono<Void>> completeFunction;
    private final Function<MessageLockToken, Mono<Void>> onAbandon;
    private final Function<MessageLockToken, Mono<Instant>> onRenewLock;
    private final Deque<ServiceBusReceivedMessage> messageQueue = new ConcurrentLinkedDeque<>();
    private final boolean isAutoRenewLock;
    private final Duration maxAutoLockRenewal;
    private final MessageLockContainer messageLockContainer;

    ServiceBusMessageProcessor(boolean isAutoComplete, boolean isAutoRenewLock, Duration maxAutoLockRenewal,
        AmqpRetryOptions retryOptions, MessageLockContainer messageLockContainer, AmqpErrorContext errorContext,
        Function<MessageLockToken, Mono<Void>> onComplete,
        Function<MessageLockToken, Mono<Void>> onAbandon,
        Function<MessageLockToken, Mono<Instant>> onRenewLock) {

        super();

        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.errorContext = Objects.requireNonNull(errorContext, "'errorContext' cannot be null.");
        this.completeFunction = Objects.requireNonNull(onComplete, "'onComplete' cannot be null.");
        this.onAbandon = Objects.requireNonNull(onAbandon, "'onAbandon' cannot be null.");
        this.onRenewLock = Objects.requireNonNull(onRenewLock, "'onRenewLock' cannot be null.");
        this.messageLockContainer = Objects.requireNonNull(messageLockContainer,
            "'messageLockContainer' cannot be null.");

        this.isAutoComplete = isAutoComplete;
        this.isAutoRenewLock = isAutoRenewLock;
        this.maxAutoLockRenewal = maxAutoLockRenewal;
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

    volatile Throwable error;
    static final AtomicReferenceFieldUpdater<ServiceBusMessageProcessor, Throwable> ERROR =
        AtomicReferenceFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, Throwable.class, "error");

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
    public void onNext(ServiceBusReceivedMessage message) {
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

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> downstream) {
        Objects.requireNonNull(downstream, "'downstream' cannot be null.");
        if (once == 0 && ONCE.compareAndSet(this, 0, 1)) {
            this.downstream = downstream;
            downstream.onSubscribe(this);
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

        while (true) {
            if (messageQueue.isEmpty()) {
                break;
            }

            long amountRequested = REQUESTED.get(this);
            long emitted = drainRequested(amountRequested);

            // We emitted the correct number that was requested.
            // Nothing more requested since.
            if (REQUESTED.addAndGet(this, -emitted) == 0) {
                break;
            }

            if (isDone) {
                break;
            }
        }

        if (isDone) {
            if (error != null) {
                downstream.onError(error);
            } else if (messageQueue.peekLast() == null) {
                downstream.onComplete();
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

            final ServiceBusReceivedMessage message = messageQueue.poll();
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

    private void next(ServiceBusReceivedMessage message) {
        final long sequenceNumber = message.getSequenceNumber();
        final String lockToken = message.getLockToken();
        final Instant initialLockedUntil = !CoreUtils.isNullOrEmpty(lockToken)
            ? messageLockContainer.addOrUpdate(lockToken, message.getLockedUntil())
            : message.getLockedUntil();

        if (isAutoComplete && CoreUtils.isNullOrEmpty(lockToken)) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Cannot auto-complete message without a lock token on message. Sequence number: " + sequenceNumber));
        }

        final AtomicBoolean hasError = new AtomicBoolean();
        final Disposable renewLockOperation = getRenewLockOperation(message, initialLockedUntil, hasError);

        try {
            downstream.onNext(message);
        } catch (Exception e) {
            hasError.set(true);
            logger.error("Exception occurred while handling downstream onNext operation.", e);

            if (isAutoComplete) {
                logger.info("Abandoning message lock: {}", lockToken);
                onAbandon.apply(message)
                    .onErrorContinue((error, item) -> {
                        logger.warning("Could not abandon message with lock: {}", lockToken, error);
                        setInternalError(error);
                    })
                    .doFinally(signal -> logger.info("lock[{}]. Abandon status: [{}]", lockToken, signal))
                    .block(retryOptions.getTryTimeout());
            } else {
                setInternalError(e);
            }
        } finally {
            renewLockOperation.dispose();
        }

        // An error occurred in downstream.onNext, while abandoning the message,
        // or timed out while processing. We return.
        if (hasError.get()) {
            return;
        }

        if (isAutoComplete) {
            logger.info("sequenceNumber[{}]. lock[{}]. Completing message.", sequenceNumber, lockToken);

            completeFunction.apply(message)
                .onErrorResume(error -> {
                    logger.warning("Could not complete message with lock: {}", lockToken, error);
                    setInternalError(error);
                    return Mono.empty();
                })
                .doFinally(signal -> logger.info("lock[{}]. Complete status: [{}]", lockToken, signal))
                .block(retryOptions.getTryTimeout());
        }
    }

    private Disposable getRenewLockOperation(ServiceBusReceivedMessage message, Instant initialLockedUntil,
        AtomicBoolean hasError) {

        if (!isAutoRenewLock) {
            return Disposables.disposed();
        }

        final long sequenceNumber = message.getSequenceNumber();
        final String lockToken = message.getLockToken();

        if (isAutoComplete && initialLockedUntil == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Cannot renew lock token without a value for 'message.getLockedUntil()'"));
        }

        final Instant initialRefreshDuration = initialLockedUntil.minus(Duration.ofMillis(500));
        Duration initialInterval = Duration.between(Instant.now(), initialRefreshDuration);
        if (initialInterval.isNegative()) {
            logger.info("Duration was negative. Moving to refresh immediately: {}", initialInterval.toMillis());
            initialInterval = Duration.ZERO;
        }

        logger.info("lock[{}]. lockedUntil[{}]. firstInterval[{}]. interval[{}]", lockToken, initialLockedUntil,
            initialRefreshDuration, initialInterval);

        final EmitterProcessor<Duration> emitterProcessor = EmitterProcessor.create();
        final FluxSink<Duration> sink = emitterProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

        // Adjust the interval, so we can buffer time for the time it'll take to refresh.
        sink.next(MessageUtils.adjustServerTimeout(initialInterval));

        final Disposable timeoutOperation = Mono.delay(maxAutoLockRenewal)
            .subscribe(l -> {
                if (!sink.isCancelled()) {
                    sink.error(new AmqpException(true, AmqpErrorCondition.TIMEOUT_ERROR,
                        "Could not complete within renewal time. Max renewal time: " + maxAutoLockRenewal,
                        errorContext));
                }
            });

        final Disposable renewLockSubscription = Flux.switchOnNext(emitterProcessor.map(i -> Flux.interval(i)))
            .flatMap(delay -> onRenewLock.apply(message))
            .map(instant -> {
                final Instant updated = messageLockContainer.addOrUpdate(lockToken, instant);
                final Duration next = Duration.between(Instant.now(), updated);
                logger.info("lockToken[{}]. given[{}]. updated[{}]. Next renewal: [{}]",
                    lockToken, instant, updated, next);

                sink.next(MessageUtils.adjustServerTimeout(next));
                return updated;
            })
            .subscribe(lockedUntil -> {
                logger.verbose("seq[{}]. lockToken[{}]. lockedUntil[{}]. Lock renewal successful.",
                    sequenceNumber, lockToken, lockedUntil);
            },
                error -> {
                    logger.error("Error occurred while renewing lock token.", error);
                    hasError.set(true);
                    setInternalError(error);
                },
                () -> logger.info("Renewing lock token task completed."));

        return Disposables.composite(renewLockSubscription, timeoutOperation);
    }

    private void setInternalError(Throwable error) {
        if (Exceptions.addThrowable(ERROR, this, error)) {
            isDone = true;
        } else {
            Operators.onErrorDropped(error, downstream.currentContext());
        }
    }
}
