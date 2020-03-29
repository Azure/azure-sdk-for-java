// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

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
    private final AtomicInteger counter = new AtomicInteger();

    private final Object connectionLock = new Object();
    private final AmqpRetryPolicy retryPolicy;
    private final String connectionId;
    private final String entityPath;
    private final Function<T, Flux<AmqpEndpointState>> endpointStatesFunction;

    private volatile Subscription upstream;
    private volatile ConcurrentLinkedDeque<ChannelSubscriber<T>> subscribers = new ConcurrentLinkedDeque<>();
    private volatile Throwable lastError;
    private volatile AmqpChannelSubscriber<T> currentChannel;
    private volatile String currentChannelIdentifier = null;
    private volatile AmqpChannelSubscriber<T> pendingChannel;
    private volatile String pendingChannelIdentifier = null;

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
            // Request the first connection on a subscription.
            isRequested.set(true);
            subscription.request(1);
        } else {
            final Throwable error = logger.logExceptionAsError(new IllegalStateException(String.format(
                "Processor cannot be subscribed to with multiple upstreams. connectionId: %s. entityPath: %s",
                connectionId, entityPath)));

            onError(Operators.onOperatorError(subscription, error, Context.empty()));
        }
    }

    @Override
    public void onNext(T amqpChannel) {
        Objects.requireNonNull(amqpChannel, "'amqpChannel' cannot be null.");

        if (isDisposed()) {
            logger.warning("connectionId[{}] entityPath[{}]: Cannot emit another item. Processor is closed.",
                connectionId, entityPath);

            Operators.onNextDropped(amqpChannel, Context.empty());
            return;
        }

        final String identifier = connectionId + "_" + counter.incrementAndGet();

        logger.info("connectionId[{}] entityPath[{}] subscriberId[{}]: Waiting for active status.",
            connectionId, entityPath, identifier);

        final AmqpChannelSubscriber<T> subscriber = endpointStatesFunction.apply(amqpChannel)
            .subscribeWith(new AmqpChannelSubscriber<>(identifier, amqpChannel));
        synchronized (connectionLock) {
            if (currentChannel != null) {
                currentChannel.dispose();
                currentChannel = null;
            }

            pendingChannelIdentifier = identifier;
            pendingChannel = subscriber;
        }

        isRequested.set(false);
        subscriber.isOpen().subscribe(
            e -> onEndpointActive(identifier),
            error -> onEndpointError(identifier, error),
            () -> onEndpointComplete(identifier));
    }

    @Override
    public void onError(Throwable throwable) {
        Objects.requireNonNull(throwable, "'throwable' is required.");

        if (isRetryPending.get() && retryPolicy.calculateRetryDelay(throwable, retryAttempts.get()) != null) {
            logger.warning("connectionId[{}]: Retry is already pending. Ignoring transient error.",
                connectionId, throwable);
            return;
        }

        int attemptsMade = retryAttempts.incrementAndGet();

        if (throwable instanceof AmqpException) {
            AmqpException amqpException = (AmqpException) throwable;
            // Connection processor should never be disposed if the underlying error is transient.
            // So, we never exhaust the retry attempts for transient errors. This will ensure a new connection
            // will be created whenever the underlying transient error is resolved. For e.g. when a network
            // connection is lost for an extended period of time and when the network is restored later, we should be
            // able to recreate a new AMQP connection.
            if (amqpException.isTransient()) {
                logger.verbose("Attempted {} times to get a new AMQP connection", attemptsMade);
                // for the purpose of computing delay, we'll use the min of retry attempts or max retries set in
                // the retry policy to get the max delay duration.
                attemptsMade = Math.min(attemptsMade, retryPolicy.getMaxRetries());
            }
        }
        final int attempt = attemptsMade;
        final Duration retryInterval = retryPolicy.calculateRetryDelay(throwable, attempt);

        if (retryInterval != null) {
            // There was already a retry in progress, so we decrement the value because we don't want to make two retry
            // attempts concurrently.
            if (isRetryPending.getAndSet(true)) {
                retryAttempts.decrementAndGet();
                return;
            }

            logger.warning("connectionId[{}]: Retry #{}. Transient error occurred. Retrying after {} ms.",
                connectionId, attempt, retryInterval.toMillis(), throwable);

            retrySubscription = Mono.delay(retryInterval).subscribe(i -> {
                if (isDisposed()) {
                    logger.info("connectionId[{}]: Retry #{}. Not requesting from upstream. Processor is disposed.",
                        connectionId, attempt);
                } else {
                    logger.info("connectionId[{}]: Retry #{}. Requesting from upstream.", connectionId, attempt);

                    requestUpstream();
                    isRetryPending.set(false);
                }
            });

            return;
        }

        logger.warning("connectionId[{}]: Non-retryable error occurred in connection.", connectionId, throwable);
        lastError = throwable;
        isDisposed.set(true);
        dispose();

        synchronized (connectionLock) {
            final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscriber -> subscriber.onError(throwable));
        }
    }

    /**
     * Upstream completed, so we terminate any subscribers waiting for a current connection. We don't close the existing
     * connection (currentChannel) though.
     */
    @Override
    public void onComplete() {
        logger.info("connectionId[{}]: Upstream connection publisher was completed. Terminating processor.",
            connectionId);

        // Already completed the processor before. Don't do it again.
        if (isDisposed.getAndSet(true)) {
            return;
        }

        synchronized (connectionLock) {
            final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscriber -> subscriber.onComplete());
        }
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        if (isDisposed()) {
            if (lastError != null) {
                actual.onSubscribe(Operators.emptySubscription());
                actual.onError(lastError);
            } else if (currentChannel == null) {
                Operators.error(actual, logger.logExceptionAsError(new IllegalStateException(
                    String.format("connectionId[%s] entityPath[%s]: Cannot subscribe. Processor is already terminated.",
                        connectionId, entityPath))));
            }

            return;
        }

        final ChannelSubscriber<T> subscriber = new ChannelSubscriber<>(actual, this);
        actual.onSubscribe(subscriber);

        synchronized (connectionLock) {
            if (currentChannel != null) {
                subscriber.complete(currentChannel.getChannel());
                return;
            }

            subscribers.add(subscriber);
        }

        if (!isDisposed() && !isRetryPending.get()) {
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

        synchronized (connectionLock) {
            if (currentChannel != null) {
                currentChannel.dispose();
                currentChannel = null;
            }

            if (pendingChannel != null) {
                pendingChannel.dispose();
                pendingChannel = null;
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    private void requestUpstream() {
        if (isDisposed()) {
            logger.warning("connectionId[{}] entityPath[{}]: Is already disposed.", connectionId, entityPath);
        }

        synchronized (connectionLock) {
            if (currentChannel != null || pendingChannel != null) {
                return;
            }
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

    private void onEndpointComplete(String identifier) {
        Objects.requireNonNull(identifier, "'identifier' cannot be null.");

        synchronized (connectionLock) {
            // They are the same instance. Ignore this status change.
            if (identifier.equals(currentChannelIdentifier)) {
                logger.info("connectionId[{}] entityPath[{}]: Current channel is closed.", connectionId, entityPath);
                currentChannel.dispose();
                currentChannel = null;
                currentChannelIdentifier = null;
            } else if (identifier.equals(pendingChannelIdentifier)) {
                logger.info("connectionId[{}] entityPath[{}]: Pending channel is closed.", connectionId, entityPath);
                pendingChannel.dispose();
                pendingChannel = null;
                pendingChannelIdentifier = null;
            } else {
                logger.warning("identifier[{}]: Unknown channel is closed. Skipping.", identifier);
            }
        }

        requestUpstream();
    }

    private void onEndpointError(String identifier, Throwable error) {
        Objects.requireNonNull(identifier, "'identifier' cannot be null.");

        final AmqpChannelSubscriber<T> oldChannel;
        synchronized (connectionLock) {
            if (identifier.equals(currentChannelIdentifier)) {
                logger.warning("identifier[{}] Current connection encountered an error.", identifier, error);
                oldChannel = currentChannel;
                currentChannel = null;
            } else if (identifier.equals(pendingChannelIdentifier)) {
                logger.warning("identifier[{}] Pending connection encountered an error.", identifier, error);
                oldChannel = pendingChannel;
                pendingChannel = null;
            } else {
                logger.warning("identifier[{}] Unknown connection encountered an error.", identifier, error);
                oldChannel = null;
            }
        }

        if (oldChannel != null) {
            oldChannel.dispose();
        }

        onError(error);
    }

    private void onEndpointActive(String identifier) {
        Objects.requireNonNull(identifier, "'identifier' cannot be null.");
        synchronized (connectionLock) {
            // They are the same instance. Ignore this status change.
            if (identifier.equals(currentChannelIdentifier)) {
                logger.info("currentChannel is pointing to pendingChannel. Skipping.");
            } else if (identifier.equals(pendingChannelIdentifier)) {
                logger.info("connectionId[{}] entityPath[{}]: Setting active AMQP channel.", connectionId, entityPath);
                currentChannel = pendingChannel;
                currentChannelIdentifier = pendingChannelIdentifier;

                pendingChannel = null;
                pendingChannelIdentifier = null;

                final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
                subscribers = new ConcurrentLinkedDeque<>();
                currentSubscribers.forEach(subscription -> subscription.onNext(currentChannel.getChannel()));

                retryAttempts.set(0);
            } else {
                logger.warning("Unknown identifier [{}] notified of onEndpointActive. Skipping.", identifier);
            }
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
