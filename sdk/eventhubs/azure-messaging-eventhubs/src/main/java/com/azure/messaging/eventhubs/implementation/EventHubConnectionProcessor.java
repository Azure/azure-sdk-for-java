// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subscribes to an upstream Mono that creates {@link EventHubAmqpConnection} then publishes the created connection
 * until it closes then recreates it.
 */
public class EventHubConnectionProcessor extends Mono<EventHubAmqpConnection>
    implements Processor<EventHubAmqpConnection, EventHubAmqpConnection>, CoreSubscriber<EventHubAmqpConnection>,
    Disposable {

    private final ClientLogger logger = new ClientLogger(EventHubConnectionProcessor.class);
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final AtomicBoolean isRequested = new AtomicBoolean();
    private final EmitterProcessor<Duration> durationSource = EmitterProcessor.create();
    private final FluxSink<Duration> durationSourceSink = durationSource.sink(FluxSink.OverflowStrategy.BUFFER);
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final Object lock = new Object();
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final AmqpRetryOptions retryOptions;
    private final AmqpRetryPolicy retryPolicy;
    private final Disposable retrySubscription;

    private Subscription upstream;
    private EventHubAmqpConnection currentConnection;

    private volatile ConcurrentLinkedDeque<ConnectionSubscriber> subscribers = new ConcurrentLinkedDeque<>();
    private volatile Throwable lastError;
    private volatile Disposable connectionSubscription;

    public EventHubConnectionProcessor(String fullyQualifiedNamespace, String eventHubName,
        AmqpRetryOptions retryOptions) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");

        this.retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
        this.retrySubscription = Flux.switchOnNext(durationSource.map(duration -> Mono.delay(duration)))
            .subscribe(unused -> {
                if (upstream != null) {
                    upstream.request(1);
                } else {
                    logger.info("No subscription to request.");
                }
            });
    }

    /**
     * Gets the fully qualified namespace for the connection.
     *
     * @return The fully qualified namespace this is connection.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the name of the Event Hub.
     *
     * @return The name of the Event Hub.
     */
    public String getEventHubName() {
        return eventHubName;
    }

    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        logger.verbose("Subscribing to upstream for connections.");

        this.upstream = subscription;

        // Don't request an EventHubAmqpConnection until there is a subscriber.
        subscription.request(0);
    }

    @Override
    public void onNext(EventHubAmqpConnection eventHubAmqpConnection) {
        logger.info("Setting next AMQP connection.");

        Objects.requireNonNull(eventHubAmqpConnection, "'eventHubAmqpConnection' cannot be null.");

        final EventHubAmqpConnection oldConnection;
        final Disposable oldSubscription;
        synchronized (lock) {
            oldConnection = currentConnection;
            oldSubscription = connectionSubscription;

            currentConnection = eventHubAmqpConnection;

            final ConcurrentLinkedDeque<ConnectionSubscriber> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscription -> subscription.onNext(eventHubAmqpConnection));

            connectionSubscription = eventHubAmqpConnection.getEndpointStates().subscribe(
                state -> {
                    // Connection was successfully opened, we can reset the retry interval.
                    if (state == AmqpEndpointState.ACTIVE) {
                        retryAttempts.set(0);
                    }
                },
                error -> {
                    synchronized (lock) {
                        currentConnection = null;
                    }
                    onError(error);
                },
                () -> {
                    if (isDisposed()) {
                        logger.info("Connection is disposed. Not requesting another connection.");
                    } else {
                        logger.info("Connection closed. Requesting another.");
                        upstream.request(1);
                    }
                });
        }

        if (oldConnection != null) {
            oldConnection.close();
        }

        if (oldSubscription != null) {
            oldSubscription.dispose();
        }

        isRequested.set(false);
    }

    @Override
    public void onError(Throwable throwable) {
        Objects.requireNonNull(throwable, "'throwable' is required.");

        final int attempt = retryAttempts.incrementAndGet();
        final Duration retryInterval = retryPolicy.calculateRetryDelay(throwable, attempt);

        if (retryInterval != null) {
            logger.warning("Transient error occurred. Attempt: {}. Retrying after {} ms.",
                attempt, retryInterval.toMillis(), throwable);

            durationSourceSink.next(retryInterval);
            return;
        }

        logger.warning("Non-retryable error occurred in connection.", throwable);
        lastError = throwable;
        isTerminated.set(true);
        dispose();

        synchronized (lock) {
            final ConcurrentLinkedDeque<ConnectionSubscriber> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscriber -> subscriber.onError(throwable));
        }
    }

    @Override
    public void onComplete() {
        logger.info("Upstream connection publisher was completed. Terminating processor.");

        isTerminated.set(true);
        synchronized (lock) {
            final ConcurrentLinkedDeque<ConnectionSubscriber> currentSubscribers = subscribers;
            subscribers = new ConcurrentLinkedDeque<>();

            currentSubscribers.forEach(subscriber -> subscriber.onComplete());
        }
    }

    @Override
    public void subscribe(CoreSubscriber<? super EventHubAmqpConnection> actual) {
        logger.verbose("Subscription received.");

        final ConnectionSubscriber subscriber = new ConnectionSubscriber(actual, this);
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
            if (currentConnection != null) {
                subscriber.complete(currentConnection);
                return;
            }

            subscribers.add(subscriber);

            if (!isRequested.getAndSet(true)) {
                logger.info("Connection not obtained yet. Requesting one.");
                upstream.request(1);
            }
        }
    }

    @Override
    public void dispose() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        retrySubscription.dispose();
        onComplete();

        synchronized (lock) {
            if (currentConnection != null) {
                currentConnection.close();
            }

            currentConnection = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return isTerminated.get();
    }

    /**
     * Represents a subscriber, waiting for an AMQP connection.
     */
    private static class ConnectionSubscriber
        extends Operators.MonoSubscriber<EventHubAmqpConnection, EventHubAmqpConnection> {
        private final EventHubConnectionProcessor processor;

        private ConnectionSubscriber(CoreSubscriber<? super EventHubAmqpConnection> actual,
            EventHubConnectionProcessor processor) {
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
        public void onNext(EventHubAmqpConnection connection) {
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
