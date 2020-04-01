// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.Messages;
import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.time.Duration;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Processes AMQP receive links into a stream of AMQP messages.
 */
public class AmqpReceiveLinkProcessor extends FluxProcessor<AmqpReceiveLink, Message> implements Subscription {
    // We don't want to dump too many credits on the link at once. It's easy enough to ask for more.
    private static final int MINIMUM_REQUEST = 0;
    private static final int MAXIMUM_REQUEST = 100;

    private final ClientLogger logger = new ClientLogger(AmqpReceiveLinkProcessor.class);
    private final Object lock = new Object();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final AtomicBoolean hasDownstream = new AtomicBoolean();
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final AtomicInteger linkCreditRequest = new AtomicInteger(1);
    private final Deque<Message> messageQueue = new ConcurrentLinkedDeque<>();

    private final int prefetch;
    private final AmqpRetryPolicy retryPolicy;
    private Disposable parentConnection;

    private volatile Subscription upstream;
    private volatile CoreSubscriber<? super Message> downstream;
    private volatile boolean isCancelled;

    private volatile Throwable lastError;
    private volatile AmqpReceiveLink currentLink;
    private volatile Disposable currentLinkSubscriptions;
    private volatile Disposable retrySubscription;

    volatile int wip;
    static final AtomicIntegerFieldUpdater<AmqpReceiveLinkProcessor> WIP =
        AtomicIntegerFieldUpdater.newUpdater(AmqpReceiveLinkProcessor.class, "wip");

    /**
     * Creates an instance of {@link AmqpReceiveLinkProcessor}.
     *
     * @param prefetch The number if messages to initially fetch.
     * @param retryPolicy Retry policy to apply when fetching a new AMQP channel.
     * @param parentConnection Represents the parent connection.
     *
     * @throws NullPointerException if {@code retryPolicy} is null.
     * @throws IllegalArgumentException if {@code prefetch} is less than 0.
     */
    public AmqpReceiveLinkProcessor(int prefetch, AmqpRetryPolicy retryPolicy, Disposable parentConnection) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.parentConnection = Objects.requireNonNull(parentConnection, "'parentConnection' cannot be null.");

        if (prefetch < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'prefetch' cannot be less than 0."));
        }

        this.prefetch = prefetch;
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
        return isTerminated.get() || isCancelled;
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

        this.upstream = subscription;
        requestUpstream();
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
            logger.warning("linkName[{}] entityPath[{}]. Got another link when we have already terminated processor.",
                next.getLinkName(), next.getEntityPath());
            return;
        }

        final String linkName = next.getLinkName();
        final String entityPath = next.getEntityPath();

        logger.info("linkName[{}] entityPath[{}]. Setting next AMQP receive link.", linkName, entityPath);

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
                    logger.verbose("linkName[{}] entityPath[{}]. Emitter has no downstream subscribers."
                        + " Not adding credits.", linkName, entityPath);

                    return 0;
                }
            });

            currentLinkSubscriptions = Disposables.composite(
                next.getEndpointStates().subscribe(
                    state -> {
                        // Connection was successfully opened, we can reset the retry interval.
                        if (state == AmqpEndpointState.ACTIVE) {
                            retryAttempts.set(0);
                        }
                    },
                    error -> {
                        currentLink = null;
                        logger.warning("linkName[{}] entityPath[{}]. Error occurred in in link.", linkName, entityPath);
                        onError(error);
                    },
                    () -> {
                        if (parentConnection.isDisposed()) {
                            logger.info("linkName[{}] entityPath[{}]. Parent connection is disposed.",
                                linkName, entityPath);
                        } else if (isTerminated()) {
                            logger.info("linkName[{}] entityPath[{}]. Processor is disposed.", linkName, entityPath);
                        } else {
                            logger.info("linkName[{}] entityPath[{}]. Receive link endpoint states are closed.",
                                linkName, entityPath);

                            final AmqpReceiveLink existing = currentLink;
                            currentLink = null;

                            if (existing != null) {
                                existing.dispose();
                            }

                            requestUpstream();
                        }
                    }),
                next.receive().subscribe(message -> {
                    messageQueue.add(message);
                    drain();
                }));
        }

        if (oldChannel != null) {
            oldChannel.dispose();
        }

        if (oldSubscription != null) {
            oldSubscription.dispose();
        }
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
            final String linkName;
            final String entityPath;
            synchronized (lock) {
                linkName = currentLink != null ? currentLink.getLinkName() : "n/a";
                entityPath = currentLink != null ? currentLink.getEntityPath() : "n/a";
            }

            logger.info("linkName[{}] entityPath[{}]. AmqpReceiveLink is already terminated.", linkName, entityPath);

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
            drain();
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

        if (isTerminated() || isCancelled) {
            logger.info("AmqpReceiveLinkProcessor is terminated. Not reopening on error.");
            Operators.onErrorDropped(throwable, currentContext());
            return;
        }

        drain();

        final int attempt = retryAttempts.incrementAndGet();
        final Duration retryInterval = retryPolicy.calculateRetryDelay(throwable, attempt);

        final String linkName;
        final String entityPath;
        synchronized (lock) {
            linkName = currentLink != null ? currentLink.getLinkName() : "n/a";
            entityPath = currentLink != null ? currentLink.getEntityPath() : "n/a";
        }

        if (retryInterval != null && !parentConnection.isDisposed()) {
            logger.warning("linkName[{}] entityPath[{}]. Transient error occurred. Attempt: {}. Retrying after {} ms.",
                linkName, entityPath, attempt, retryInterval.toMillis(), throwable);

            retrySubscription = Mono.delay(retryInterval).subscribe(i -> requestUpstream());

            return;
        }

        if (parentConnection.isDisposed()) {
            logger.info("Parent connection is disposed. Not reopening on error.");
        }

        logger.warning("linkName[{}] entityPath[{}] Non-retryable error occurred in AMQP receive link.",
            linkName, entityPath, throwable);

        lastError = throwable;

        isTerminated.set(true);

        synchronized (lock) {
            if (downstream != null) {
                downstream.onError(throwable);
            }
        }

        onDispose();
    }

    /**
     * When the upstream publisher has no more items to emit.
     */
    @Override
    public void onComplete() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        drain();
        onDispose();
    }

    @Override
    public void dispose() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        drain();
        onDispose();
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

        logger.verbose("Back pressure request. Old value: {}. New value: {}", linkCreditRequest.get(), newRequest);
        linkCreditRequest.set(newRequest);
        drain();
    }

    /**
     * When downstream subscriber cancels their subscription.
     */
    @Override
    public void cancel() {
        if (isCancelled) {
            return;
        }

        isCancelled = true;
        drain();
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

        logger.info("Requesting for a new AmqpReceiveLink from upstream.");
        upstream.request(1);
    }

    private void onDispose() {
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

        final Message lastMessage = messageQueue.peekLast();
        if (lastMessage == null) {
            if (isTerminated() || isCancelled) {
                downstream.onComplete();

                if (currentLink != null) {
                    currentLink.dispose();
                }
            }

            return;
        }

        Message message = messageQueue.poll();
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

        if (isTerminated() || isCancelled) {
            if (lastError != null) {
                downstream.onError(lastError);
            } else if (messageQueue.peekLast() == null) {
                downstream.onComplete();
            }

            if (currentLink != null) {
                currentLink.dispose();
            }
        }
    }

    private void next(Message message) {
        try {
            downstream.onNext(message);
        } catch (Exception e) {
            logger.error("Exception occurred while handling downstream onNext operation.", e);
            throw logger.logExceptionAsError(Exceptions.propagate(
                Operators.onOperatorError(upstream, e, message, downstream.currentContext())));
        }
    }
}
