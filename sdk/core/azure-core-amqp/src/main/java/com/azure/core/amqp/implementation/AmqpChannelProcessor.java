// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

public class AmqpChannelProcessor<T> extends Mono<T> implements Processor<T, T>, CoreSubscriber<T>, Disposable {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<AmqpChannelProcessor, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(AmqpChannelProcessor.class, Subscription.class,
            "upstream");

    private final ClientLogger logger;
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicBoolean isRequested = new AtomicBoolean();
    private final AtomicBoolean isRetryPending = new AtomicBoolean();
    private final AtomicInteger retryAttempts = new AtomicInteger();

    private final Object lock = new Object();
    private final AmqpRetryPolicy retryPolicy;
    private final String connectionId;
    private final String entityPath;
    private final Function<T, Flux<AmqpEndpointState>> endpointStatesFunction;

    private volatile Subscription upstream;
    private volatile ConcurrentLinkedDeque<ChannelSubscriber<T>> subscribers = new ConcurrentLinkedDeque<>();
    private volatile Throwable lastError;
    private volatile T currentChannel;
    private volatile Disposable connectionSubscription;
    private volatile Disposable retrySubscription;

    public AmqpChannelProcessor(String connectionId, String entityPath,
        Function<T, Flux<AmqpEndpointState>> endpointStatesFunction, AmqpRetryPolicy retryPolicy, ClientLogger logger) {
        this.connectionId = Objects.requireNonNull(connectionId, "'connectionId' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.endpointStatesFunction = Objects.requireNonNull(endpointStatesFunction,
            "'endpointStates' cannot be null.");
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.logger = Objects.requireNonNull(logger, "'logger' cannot be null.");
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        if (Operators.setOnce(UPSTREAM, this, subscription)) {
            // Don't request an item until there is a subscriber.
            subscription.request(0);
        } else {
            logger.warning("Processors can only be subscribed to once.");
        }
    }

    @Override
    public void onNext(T amqpChannel) {
        logger.info("connectionId[{}] entityPath[{}]: Setting next AMQP channel.", connectionId, entityPath);

        Objects.requireNonNull(amqpChannel, "'amqpChannel' cannot be null.");

        final T oldChannel;
        final Disposable oldSubscription;
        synchronized (lock) {
            oldChannel = currentChannel;
            oldSubscription = connectionSubscription;

            currentChannel = amqpChannel;

            final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscription -> subscription.onNext(amqpChannel));

            connectionSubscription = endpointStatesFunction.apply(amqpChannel).subscribe(
                state -> {
                    // Connection was successfully opened, we can reset the retry interval.
                    if (state == AmqpEndpointState.ACTIVE) {
                        retryAttempts.set(0);
                    }
                },
                error -> {
                    setAndClearChannel();
                    onError(error);
                },
                () -> {
                    if (isDisposed()) {
                        logger.info("Channel is disposed.");
                    } else {
                        logger.info("Channel closed.");
                        setAndClearChannel();
                    }
                });
        }

        close(oldChannel);

        if (oldSubscription != null) {
            oldSubscription.dispose();
        }

        isRequested.set(false);
    }

    @Override
    public void onError(Throwable throwable) {
        Objects.requireNonNull(throwable, "'throwable' is required.");

        if (isRetryPending.get() && retryPolicy.calculateRetryDelay(throwable, retryAttempts.get()) != null) {
            logger.warning("Retry is already pending. Ignoring transient error.", throwable);
            return;
        }

        final int attempt = retryAttempts.incrementAndGet();
        final Duration retryInterval = retryPolicy.calculateRetryDelay(throwable, attempt);

        if (retryInterval != null) {
            // There was already a retry in progress, so we decrement the value because we don't want to make two retry
            // attempts concurrently.
            if (isRetryPending.getAndSet(true)) {
                retryAttempts.decrementAndGet();
                return;
            }

            logger.warning("Retry #{}. Transient error occurred. Retrying after {} ms.",
                attempt, retryInterval.toMillis(), throwable);

            retrySubscription = Mono.delay(retryInterval).subscribe(i -> {
                if (isDisposed()) {
                    logger.info("Retry #{}. Not requesting from upstream. Processor is disposed.");
                } else {
                    logger.info("Retry #{}. Requesting from upstream.", attempt);

                    requestUpstream();
                    isRetryPending.set(false);
                }
            });

            return;
        }

        logger.warning("Non-retryable error occurred in connection.", throwable);
        lastError = throwable;
        isDisposed.set(true);
        dispose();

        synchronized (lock) {
            final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscriber -> subscriber.onError(throwable));
        }
    }

    @Override
    public void onComplete() {
        logger.info("Upstream connection publisher was completed. Terminating processor.");

        isDisposed.set(true);
        synchronized (lock) {
            final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscriber -> subscriber.onComplete());
        }
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        if (isDisposed()) {
            logger.info("connectionId[{}] entityPath[{}]: Processor is already terminated.", connectionId, entityPath);
            actual.onSubscribe(Operators.emptySubscription());

            if (lastError != null) {
                actual.onError(lastError);
            } else {
                actual.onComplete();
            }

            return;
        }

        final ChannelSubscriber<T> subscriber = new ChannelSubscriber<T>(actual, this);
        actual.onSubscribe(subscriber);

        synchronized (lock) {
            if (currentChannel != null) {
                subscriber.complete(currentChannel);
                return;
            }
        }

        subscribers.add(subscriber);

        if (!isRetryPending.get()) {
            requestUpstream();
        }
    }

    @Override
    public void dispose() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        if (retrySubscription != null && !retrySubscription.isDisposed()) {
            retrySubscription.dispose();
        }

        onComplete();

        synchronized (lock) {
            setAndClearChannel();
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    private void requestUpstream() {
        if (currentChannel != null) {
            logger.verbose("connectionId[{}] entityPath[{}]: Connection exists, not requesting another.",
                connectionId, entityPath);
            return;
        } else if (isDisposed()) {
            logger.verbose("connectionId[{}] entityPath[{}]: Is already disposed.", connectionId, entityPath);
            return;
        }

        final Subscription subscription = UPSTREAM.get(this);
        if (subscription == null) {
            logger.warning("connectionId[{}] entityPath[{}]: There is no upstream subscription.",
                connectionId, entityPath);
            return;
        }

        // subscribe(CoreSubscriber) may have requested a subscriber already.
        if (!isRequested.getAndSet(true)) {
            logger.info("connectionId[{}] entityPath[{}]: Connection not requested, yet. Requesting one.",
                connectionId, entityPath);
            subscription.request(1);
        }
    }

    private void setAndClearChannel() {
        T oldChannel;
        synchronized (lock) {
            oldChannel = currentChannel;
            currentChannel = null;
        }

        close(oldChannel);
    }

    private void close(T channel) {
        if (channel instanceof AutoCloseable) {
            try {
                ((AutoCloseable) channel).close();
            } catch (Exception error) {
                logger.warning("Error occurred closing item.", channel);
            }
        } else if (channel instanceof Disposable) {
            ((Disposable) channel).dispose();
        }
    }

    /**
     * Represents a subscriber, waiting for an AMQP connection.
     */
    private static final class ChannelSubscriber<T> extends Operators.MonoSubscriber<T, T> {
        private final AmqpChannelProcessor<T> processor;

        private ChannelSubscriber(CoreSubscriber<? super T> actual, AmqpChannelProcessor<T> processor) {
            super(actual);
            this.processor = processor;
        }

        @Override
        public void cancel() {
            super.cancel();
            processor.subscribers.remove(this);
        }

        @Override
        public void onComplete() {
            if (!isCancelled()) {
                actual.onComplete();
            }
        }

        @Override
        public void onNext(T channel) {
            if (!isCancelled()) {
                super.complete(channel);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if (!isCancelled()) {
                actual.onError(throwable);
            } else {
                Operators.onOperatorError(throwable, currentContext());
            }
        }
    }
}
