/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final Object lock = new Object();
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final AmqpRetryOptions retryOptions;

    private Subscription upstream;
    private EventHubAmqpConnection currentConnection;
    private Disposable connectionSubscription;

    private volatile ConcurrentLinkedDeque<ConnectionSubscriber> subscribers = new ConcurrentLinkedDeque<>();
    private volatile Throwable lastError;

    public EventHubConnectionProcessor(String fullyQualifiedNamespace, String eventHubName,
        AmqpRetryOptions retryOptions) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
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
                },
                error -> onError(error),
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

        if (throwable instanceof AmqpException && ((AmqpException) throwable).isTransient()) {
            logger.warning("Transient occurred in connection. Retrying.", throwable);
            upstream.request(1);
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
                logger.info("Existing connection exists. Exposing that one.");
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
                super.onNext(connection);
                actual.onNext(connection);
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
