// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.AsyncCloseable;
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
import java.util.concurrent.RejectedExecutionException;
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
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final Function<T, Flux<AmqpEndpointState>> endpointStatesFunction;
    private final AmqpErrorContext errorContext;

    private volatile Subscription upstream;
    private volatile ConcurrentLinkedDeque<ChannelSubscriber<T>> subscribers = new ConcurrentLinkedDeque<>();
    private volatile Throwable lastError;
    private volatile T currentChannel;
    private volatile Disposable connectionSubscription;
    private volatile Disposable retrySubscription;

    public AmqpChannelProcessor(String fullyQualifiedNamespace, String entityPath,
        Function<T, Flux<AmqpEndpointState>> endpointStatesFunction, AmqpRetryPolicy retryPolicy, ClientLogger logger) {
        this.fullyQualifiedNamespace = Objects
            .requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.endpointStatesFunction = Objects.requireNonNull(endpointStatesFunction,
            "'endpointStates' cannot be null.");
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.logger = Objects.requireNonNull(logger, "'logger' cannot be null.");
        this.errorContext = new AmqpErrorContext(fullyQualifiedNamespace);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        if (Operators.setOnce(UPSTREAM, this, subscription)) {
            // Request the first connection on a subscription.
            isRequested.set(true);
            subscription.request(1);
        } else {
            logger.warning("Processors can only be subscribed to once.");
        }
    }

    @Override
    public void onNext(T amqpChannel) {
        logger.info("namespace[{}] entityPath[{}]: Setting next AMQP channel.", fullyQualifiedNamespace, entityPath);

        Objects.requireNonNull(amqpChannel, "'amqpChannel' cannot be null.");

        final T oldChannel;
        final Disposable oldSubscription;
        synchronized (lock) {
            oldChannel = currentChannel;
            oldSubscription = connectionSubscription;

            currentChannel = amqpChannel;

            final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
            logger.info("namespace[{}] entityPath[{}]: Next AMQP channel received, updating {} current "
                + "subscribers", fullyQualifiedNamespace, entityPath, subscribers.size());

            currentSubscribers.forEach(subscription -> subscription.onNext(amqpChannel));

            connectionSubscription = endpointStatesFunction.apply(amqpChannel).subscribe(
                state -> {
                    // Connection was successfully opened, we can reset the retry interval.
                    if (state == AmqpEndpointState.ACTIVE) {
                        retryAttempts.set(0);
                        logger.info("namespace[{}] entityPath[{}]: Channel is now active.",
                            fullyQualifiedNamespace, entityPath);
                    }
                },
                error -> {
                    setAndClearChannel();
                    onError(error);
                },
                () -> {
                    if (isDisposed()) {
                        logger.info("namespace[{}] entityPath[{}]: Channel is disposed.",
                            fullyQualifiedNamespace, entityPath);
                    } else {
                        logger.info("namespace[{}] entityPath[{}]: Channel is closed. Requesting upstream. ",
                            fullyQualifiedNamespace, entityPath);
                        setAndClearChannel();
                        requestUpstream();
                    }
                });
        }

        close(oldChannel);

        if (oldSubscription != null) {
            oldSubscription.dispose();
        }

        isRequested.set(false);
    }

    /**
     * When downstream or upstream encounters an error, calculates whether to request another item upstream.
     *
     * @param throwable Exception to analyse.
     */
    @Override
    public void onError(Throwable throwable) {
        Objects.requireNonNull(throwable, "'throwable' is required.");

        if (isRetryPending.get() && retryPolicy.calculateRetryDelay(throwable, retryAttempts.get()) != null) {
            logger.warning("Retry is already pending. Ignoring transient error.", throwable);
            return;
        }

        final int attemptsMade = retryAttempts.incrementAndGet();
        final int attempts;
        final Duration retryInterval;

        // Connection processor should never be disposed if the underlying error is transient.
        // So, we never exhaust the retry attempts for transient errors. This will ensure a new connection
        // will be created whenever the underlying transient error is resolved. For e.g. when a network
        // connection is lost for an extended period of time and when the network is restored later, we should be
        // able to recreate a new AMQP connection.
        //
        // There are exceptions that will not be AmqpExceptions like IllegalStateExceptions or
        // RejectedExecutionExceptions when attempting an operation that is closed or if the IO signal is accidentally
        // closed. In these cases, we want to re-attempt the operation.
        if (((throwable instanceof AmqpException) && ((AmqpException) throwable).isTransient())
            || (throwable instanceof IllegalStateException)
            || (throwable instanceof RejectedExecutionException)) {
            // for the purpose of computing delay, we'll use the min of retry attempts or max retries set in
            // the retry policy to get the max delay duration.
            attempts = Math.min(attemptsMade, retryPolicy.getMaxRetries());

            final Throwable throwableToUse = throwable instanceof AmqpException
                ? throwable
                : new AmqpException(true, "Non-AmqpException occurred upstream.", throwable, errorContext);

            retryInterval = retryPolicy.calculateRetryDelay(throwableToUse, attempts);
        } else {
            attempts = attemptsMade;
            retryInterval = retryPolicy.calculateRetryDelay(throwable, attempts);
        }

        if (retryInterval != null) {
            // There was already a retry in progress, so we decrement the value because we don't want to make two retry
            // attempts concurrently.
            if (isRetryPending.getAndSet(true)) {
                retryAttempts.decrementAndGet();
                return;
            }

            logger.info("Retry #{}. Transient error occurred. Retrying after {} ms.", attempts,
                retryInterval.toMillis(), throwable);

            retrySubscription = Mono.delay(retryInterval).subscribe(i -> {
                if (isDisposed()) {
                    logger.info("Retry #{}. Not requesting from upstream. Processor is disposed.", attempts);
                } else {
                    logger.info("Retry #{}. Requesting from upstream.", attempts);

                    requestUpstream();
                    isRetryPending.set(false);
                }
            });
        } else {
            logger.warning("entityPath[{}] Retry #{}. Retry attempts exhausted or exception was not retriable.",
                entityPath, attempts, throwable);

            lastError = throwable;
            isDisposed.set(true);
            dispose();

            synchronized (lock) {
                final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
                subscribers = new ConcurrentLinkedDeque<>();
                logger.info("namespace[{}] entityPath[{}]: Error in AMQP channel processor. Notifying {} "
                    + "subscribers.", fullyQualifiedNamespace, entityPath, currentSubscribers.size());

                currentSubscribers.forEach(subscriber -> subscriber.onError(throwable));
            }
        }
    }

    @Override
    public void onComplete() {
        logger.info("Upstream connection publisher was completed. Terminating processor.");

        isDisposed.set(true);
        synchronized (lock) {
            final ConcurrentLinkedDeque<ChannelSubscriber<T>> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();
            logger.info("namespace[{}] entityPath[{}]: AMQP channel processor completed. Notifying {} "
                + "subscribers.", fullyQualifiedNamespace, entityPath, currentSubscribers.size());
            currentSubscribers.forEach(subscriber -> subscriber.onComplete());
        }
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        if (isDisposed()) {
            if (lastError != null) {
                actual.onSubscribe(Operators.emptySubscription());
                actual.onError(lastError);
            } else {
                Operators.error(actual, logger.logExceptionAsError(new IllegalStateException(
                    String.format("namespace[%s] entityPath[%s]: Cannot subscribe. Processor is already terminated.",
                        fullyQualifiedNamespace, entityPath))));
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
        logger.verbose("Added a subscriber {} to AMQP channel processor. Total "
            + "subscribers = {}", subscriber, subscribers.size());

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
            logger.verbose("namespace[{}] entityPath[{}]: Connection exists, not requesting another.",
                fullyQualifiedNamespace, entityPath);
            return;
        } else if (isDisposed()) {
            logger.verbose("namespace[{}] entityPath[{}]: Is already disposed.", fullyQualifiedNamespace, entityPath);
            return;
        }

        final Subscription subscription = UPSTREAM.get(this);
        if (subscription == null) {
            logger.warning("namespace[{}] entityPath[{}]: There is no upstream subscription.",
                fullyQualifiedNamespace, entityPath);
            return;
        }

        // subscribe(CoreSubscriber) may have requested a subscriber already.
        if (!isRequested.getAndSet(true)) {
            logger.info("namespace[{}] entityPath[{}]: Connection not requested, yet. Requesting one.",
                fullyQualifiedNamespace, entityPath);
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

    /**
     * Checks the current state of the channel for this channel and returns true if the channel is null or if this
     * processor is disposed.
     *
     * @return true if the current channel in the processor is null or if the processor is disposed
     */
    public boolean isChannelClosed() {
        synchronized (lock) {
            return currentChannel == null || isDisposed();
        }
    }

    private void close(T channel) {
        if (channel instanceof AsyncCloseable) {
            ((AsyncCloseable) channel).closeAsync().subscribe();
        } else if (channel instanceof AutoCloseable) {
            try {
                ((AutoCloseable) channel).close();
            } catch (Exception error) {
                logger.warning("Error occurred closing AutoCloseable channel.", error);
            }
        } else if (channel instanceof Disposable) {
            try {
                ((Disposable) channel).dispose();
            } catch (Exception error) {
                logger.warning("Error occurred closing Disposable channel.", error);
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
                processor.subscribers.remove(this);
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
                processor.subscribers.remove(this);
            } else {
                Operators.onErrorDropped(throwable, currentContext());
            }
        }
    }
}
