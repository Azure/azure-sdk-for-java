// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Processes AMQP receive links into a stream of AMQP messages.
 */
public class ServiceBusReceiveLinkProcessor extends FluxProcessor<AmqpReceiveLink, Message> implements Subscription {
    // We don't want to dump too many credits on the link at once. It's easy enough to ask for more.
    private static final int MINIMUM_REQUEST = 0;
    private static final int MAXIMUM_REQUEST = 100;

    private final ClientLogger logger = new ClientLogger(ServiceBusReceiveLinkProcessor.class);
    private final Object lock = new Object();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final AtomicBoolean hasDownstream = new AtomicBoolean();
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final AtomicBoolean isRequested = new AtomicBoolean();
    private final AtomicInteger linkCreditRequest = new AtomicInteger(1);

    private final int prefetch;
    private final AmqpRetryPolicy retryPolicy;
    private Disposable parentConnection;
    private final AmqpErrorContext errorContext;

    private volatile Subscription upstream;
    private volatile CoreSubscriber<? super Message> downstream;

    private volatile Throwable lastError;
    private volatile AmqpReceiveLink currentLink;
    private volatile Disposable currentLinkSubscriptions;
    private volatile Disposable retrySubscription;

    /**
     * Creates an instance of {@link ServiceBusReceiveLinkProcessor}.
     *
     * @param prefetch The number if messages to initially fetch.
     * @param retryPolicy Retry policy to apply when fetching a new AMQP channel.
     * @param parentConnection Represents the parent connection.
     *
     * @throws NullPointerException if {@code retryPolicy} is null.
     * @throws IllegalArgumentException if {@code prefetch} is less than 0.
     */
    public ServiceBusReceiveLinkProcessor(int prefetch, AmqpRetryPolicy retryPolicy, Disposable parentConnection,
        AmqpErrorContext errorContext) {

        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.parentConnection = Objects.requireNonNull(parentConnection, "'parentConnection' cannot be null.");
        this.errorContext = errorContext;

        if (prefetch <= 0) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'prefetch' cannot be less than or equal to 0."));
        }

        this.prefetch = prefetch;
    }

    /**
     * Gets the error context associated with this link.
     *
     * @return the error context associated with this link.
     */
    public AmqpErrorContext getErrorContext() {
        return errorContext;
    }

    /**
     * Gets the error associated with this processor.
     *
     * @return Error associated with this processor. {@code null} if there is no error.
     */
    @Override
    public Throwable getError() {
        return lastError;
    }

    /**
     * Gets whether or not the processor is terminated.
     *
     * @return {@code true} if the processor has terminated; false otherwise.
     */
    @Override
    public boolean isTerminated() {
        return isTerminated.get();
    }

    /**
     * When a subscription is obtained from upstream publisher.
     *
     * @param subscription Subscription to upstream publisher.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        Objects.requireNonNull(subscription, "'subscription' cannot be null");
        if (isTerminated()) {
            return;
        }

        logger.verbose("Subscribing to upstream.");

        this.upstream = subscription;
        subscription.request(0);
    }

    /**
     * When the next AMQP link is fetched.
     *
     * @param next The next AMQP receive link.
     */
    @Override
    public void onNext(AmqpReceiveLink next) {
        Objects.requireNonNull(next, "'next' cannot be null.");

        if (isTerminated()) {
            logger.warning("Got another link when we have already terminated processor. Link: {}",
                next.getEntityPath());
            return;
        }

        logger.info("Setting next AMQP receive link.");

        final AmqpReceiveLink oldChannel;
        final Disposable oldSubscription;
        synchronized (lock) {
            oldChannel = currentLink;
            oldSubscription = currentLinkSubscriptions;

            currentLink = next;

            next.addCredits(prefetch);
            next.setEmptyCreditListener(() -> {
                if (hasDownstream.get()) {
                    return linkCreditRequest.get();
                } else {
                    logger.verbose("Emitter has no downstream subscribers. Not adding credits.");
                    return 0;
                }
            });

            currentLinkSubscriptions = Disposables.composite(
                next.receive().publishOn(Schedulers.boundedElastic()).subscribe(message -> {
                    logger.verbose("Pushing next message downstream.");
                    downstream.onNext(message);
                }),
                next.getEndpointStates().subscribe(
                    state -> {
                        // Connection was successfully opened, we can reset the retry interval.
                        if (state == AmqpEndpointState.ACTIVE) {
                            retryAttempts.set(0);
                        }
                    },
                    error -> {
                        currentLink = null;
                        onError(error);
                    },
                    () -> {
                        if (parentConnection.isDisposed()) {
                            logger.info("Parent connection is disposed.");
                        } else if (isTerminated()) {
                            logger.info("Processor is disposed.");
                        } else {
                            logger.info("Receive link endpoint states are closed.");
                            final AmqpReceiveLink existing = currentLink;
                            currentLink = null;

                            if (existing != null) {
                                existing.dispose();
                            }

                            requestUpstream();
                        }
                    }));
        }

        if (oldChannel != null) {
            oldChannel.dispose();
        }

        if (oldSubscription != null) {
            oldSubscription.dispose();
        }

        isRequested.set(false);
    }

    /**
     * Sets up the downstream subscriber.
     *
     * @param actual The downstream subscriber.
     *
     * @throws IllegalStateException if there is already a downstream subscriber.
     */
    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        Objects.requireNonNull(actual, "'actual' cannot be null.");
        if (isTerminated()) {
            logger.info("AmqpReceiveLink is already terminated.");

            actual.onSubscribe(Operators.emptySubscription());

            if (hasError()) {
                actual.onError(lastError);
            } else {
                actual.onComplete();
            }

            return;
        }

        if (!hasDownstream.getAndSet(true)) {
            this.downstream = actual;
            actual.onSubscribe(this);
            requestUpstream();
        } else {
            Operators.error(actual, logger.logExceptionAsError(new IllegalStateException(
                "There is already one downstream subscriber.'")));
        }
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

        if (isTerminated()) {
            logger.info("AmqpReceiveLinkProcessor is terminated. Not reopening on error.");
            return;
        }

        final int attempt = retryAttempts.incrementAndGet();
        final Duration retryInterval = retryPolicy.calculateRetryDelay(throwable, attempt);

        if (retryInterval != null && !parentConnection.isDisposed()) {
            logger.warning("Transient error occurred. Attempt: {}. Retrying after {} ms.",
                attempt, retryInterval.toMillis(), throwable);

            retrySubscription = Mono.delay(retryInterval).subscribe(i -> {
                requestUpstream();
            });

            return;
        }

        if (parentConnection.isDisposed()) {
            logger.info("Parent connection is disposed. Not reopening on error.");
        }

        logger.warning("Non-retryable error occurred in AMQP receive link.", throwable);
        lastError = throwable;

        isTerminated.set(true);

        synchronized (lock) {
            if (downstream != null) {
                downstream.onError(throwable);
            }
        }

        terminate();
    }

    /**
     * Terminates the
     */
    @Override
    public void onComplete() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        if (hasDownstream.get()) {
            downstream.onComplete();
        }

        terminate();
    }

    /**
     * When downstream subscriber makes a back-pressure request.
     */
    @Override
    public void request(long request) {
        if (isTerminated.get()) {
            logger.info("Cannot request more from AMQP link processor that is disposed.");
            return;
        }

        if (request < MINIMUM_REQUEST) {
            logger.warning(Messages.REQUEST_VALUE_NOT_VALID,
                MINIMUM_REQUEST, MAXIMUM_REQUEST);
            return;
        }

        final int newRequest = request > MAXIMUM_REQUEST
            ? MAXIMUM_REQUEST
            : (int) request;

        logger.verbose("Back pressure request. Old value: {}. New value: {}", linkCreditRequest.get(),
            newRequest);
        linkCreditRequest.set(newRequest);
    }

    /**
     * When downstream subscriber cancels their subscription.
     */
    @Override
    public void cancel() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        if (hasDownstream.get()) {
            downstream.onComplete();
        }

        terminate();
    }

    private void requestUpstream() {
        if (isTerminated()) {
            logger.verbose("Terminated. Not requesting another.");
            return;
        }

        synchronized (lock) {
            if (currentLink != null) {
                logger.info("AmqpReceiveLink exists, not requesting another.");
                return;
            } else if (upstream == null) {
                logger.verbose("There is no upstream. Not requesting");
                return;
            }
        }

        // subscribe(CoreSubscriber) may have requested a subscriber already.
        if (!isRequested.getAndSet(true)) {
            logger.info("AmqpReceiveLink not requested, yet. Requesting one.");
            upstream.request(1);
        } else {
            logger.info("AmqpRecieveLink already requested.");
        }
    }

    private void terminate() {
        if (retrySubscription != null && !retrySubscription.isDisposed()) {
            retrySubscription.dispose();
        }

        if (currentLink != null) {
            currentLink.dispose();
        }

        currentLink = null;

        if (currentLinkSubscriptions != null) {
            currentLinkSubscriptions.dispose();
        }
    }
}
