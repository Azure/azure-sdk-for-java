// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

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
import java.util.function.Function;

/**
 * Processor that returns an active AMQP channel when requested. While the AMQP channel is active, all downstream
 * subscribers will get the same instance.
 *
 * @param <T> Type of AMQP channel.
 */
class AmqpChannelProcessor<T extends AutoCloseable> extends Mono<T> implements Processor<T, T>,
    CoreSubscriber<T>, Disposable {
    private final ClientLogger logger;
    private final Function<T, Flux<AmqpEndpointState>> endpointStatesFunction;
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final AtomicBoolean isRequested = new AtomicBoolean();
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final Object lock = new Object();
    private final AmqpRetryPolicy retryPolicy;

    private Subscription upstream;
    private T currentChannel;

    private volatile ConcurrentLinkedDeque<InnerSubscriber> subscribers = new ConcurrentLinkedDeque<>();
    private volatile Throwable lastError;
    private volatile Disposable channelSubscription;
    private volatile Disposable retrySubscription;

    /**
     * Creates an instance of {@link AmqpChannelProcessor}.
     *
     * @param retryPolicy Retry policy to apply when fetching a new AMQP channel.
     * @param endpointStatesFunction A function that maps an AMQP channel to a stream of {@link AmqpEndpointState}
     *     that represents the channel's state.
     * @param logger Logger for client information.
     * @throws NullPointerException if {@code retryPolicy}, {@code endpointStatesFunction}, or {@code logger} are null.
     */
    AmqpChannelProcessor(AmqpRetryPolicy retryPolicy, Function<T, Flux<AmqpEndpointState>> endpointStatesFunction,
        ClientLogger logger) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.logger = Objects.requireNonNull(logger, "'logger' cannot be null.");
        this.endpointStatesFunction = Objects.requireNonNull(endpointStatesFunction,
            "'endpointStatesFunction' cannot be null.");
    }

    /**
     * When a subscription is obtained from upstream publisher.
     *
     * @param subscription Subscription to upstream publisher.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        logger.verbose("Subscribing to upstream.");

        this.upstream = subscription;

        // Don't request from upstream until there is a downstream subscriber.
        subscription.request(0);
    }

    /**
     * When the next element is received from upstream publisher. This connects the element and publishes it to
     * current and subsequent downstream subscribers until the AMQP channel is no longer active.
     *
     * @param next Next element from upstream publisher.
     * @throws NullPointerException if {@code next} is null.
     */
    @Override
    public void onNext(T next) {
        logger.info("Setting next AMQP item.");

        Objects.requireNonNull(next, "'next' cannot be null.");

        final T oldChannel;
        final Disposable oldSubscription;
        synchronized (lock) {
            oldChannel = currentChannel;
            oldSubscription = channelSubscription;

            currentChannel = next;

            final ConcurrentLinkedDeque<InnerSubscriber> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscription -> subscription.onNext(next));

            channelSubscription = endpointStatesFunction.apply(next).subscribe(
                state -> {
                    // Connection was successfully opened, we can reset the retry interval.
                    if (state == AmqpEndpointState.ACTIVE) {
                        retryAttempts.set(0);
                    }
                },
                error -> {
                    synchronized (lock) {
                        currentChannel = null;
                    }
                    onError(error);
                },
                () -> {
                    if (isDisposed()) {
                        logger.info("Connection is disposed.");
                    } else {
                        logger.info("Connection closed.");
                        currentChannel = null;
                    }
                });
        }

        if (oldChannel != null) {
            try {
                oldChannel.close();
            } catch (Exception e) {
                logger.info("Error encountered closing old connection.", e);
            }
        }

        if (oldSubscription != null) {
            oldSubscription.dispose();
        }

        isRequested.set(false);
    }

    /**
     * When an error occurs from the upstream publisher. If the {@code throwable} is a transient failure, another AMQP
     * element is requested if the {@link AmqpRetryPolicy} allows. Otherwise, the processor closes.
     *
     * @param throwable Error that occurred in upstream publisher.
     */
    @Override
    public void onError(Throwable throwable) {
        Objects.requireNonNull(throwable, "'throwable' is required.");

        final int attempt = retryAttempts.incrementAndGet();
        final Duration retryInterval = retryPolicy.calculateRetryDelay(throwable, attempt);

        if (retryInterval != null) {
            logger.warning("Transient error occurred. Attempt: {}. Retrying after {} ms.",
                attempt, retryInterval.toMillis(), throwable);

            retrySubscription = Mono.delay(retryInterval).subscribe(i -> {
                requestUpstream();
            });

            return;
        }

        logger.warning("Non-retryable error occurred in connection.", throwable);
        lastError = throwable;
        isTerminated.set(true);
        dispose();

        synchronized (lock) {
            final ConcurrentLinkedDeque<InnerSubscriber> currentSubscribers =
                subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscriber -> subscriber.onError(throwable));
        }
    }

    /**
     * When the upstream publisher has no more AMQP channel elements to emit. The processor is closed. Subsequent
     * subscribers will not get a AMQP channel.
     */
    @Override
    public void onComplete() {
        logger.info("Upstream connection publisher was completed. Terminating processor.");

        isTerminated.set(true);
        synchronized (lock) {
            final ConcurrentLinkedDeque<InnerSubscriber> currentSubscribers =
                subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscriber -> subscriber.onComplete());
        }
    }

    /**
     * When a downstream subscriber is received. This publishes either the current, active AMQP
     *
     * @param actual
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        logger.verbose("Subscription received.");

        final InnerSubscriber<T> subscriber = new InnerSubscriber<>(actual, this);
        actual.onSubscribe(subscriber);

        if (isTerminated.get()) {
            if (lastError != null) {
                logger.info("Processor is already terminated. Propagating error.");
                subscriber.onError(lastError);
            } else {
                logger.info("Processor is already terminated. Propagating complete.");
                subscriber.onComplete();
            }

            return;
        }

        synchronized (lock) {
            if (currentChannel != null) {
                subscriber.complete(currentChannel);
                return;
            }
        }

        subscribers.add(subscriber);
        requestUpstream();
    }

    @Override
    public void dispose() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        if (retrySubscription != null && !retrySubscription.isDisposed()) {
            retrySubscription.dispose();
        }

        onComplete();

        synchronized (lock) {
            if (currentChannel != null) {
                try {
                    currentChannel.close();
                } catch (Exception error) {
                    logger.warning("Error encountered closing connection.", error);
                }
            }

            currentChannel = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return isTerminated.get();
    }

    private void requestUpstream() {
        synchronized (lock) {
            if (currentChannel != null) {
                logger.info("Connection exists, not requesting another.");
                return;
            } else if (isTerminated.get() || upstream == null) {
                logger.verbose("Terminated. Not requesting another.");
                return;
            }
        }

        // subscribe(CoreSubscriber) may have requested a subscriber already.
        if (!isRequested.getAndSet(true)) {
            logger.info("Connection not requested, yet. Requesting one.");
            upstream.request(1);
        } else {
            logger.info("Retried connection already requested.");
        }
    }

    /**
     * Represents a subscriber, waiting for an AMQP connection.
     */
    private static final class InnerSubscriber<T extends AutoCloseable> extends Operators.MonoSubscriber<T, T> {
        private final AmqpChannelProcessor<T> processor;

        private InnerSubscriber(CoreSubscriber<? super T> actual,
            AmqpChannelProcessor<T> processor) {
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
        public void onNext(T connection) {
            if (!isCancelled()) {
                super.complete(connection);
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
