// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.CREDITS_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.TRACKING_ID_KEY;

/**
 * Processes AMQP receive links into a stream of AMQP messages.
 */
public class AmqpReceiveLinkProcessor extends FluxProcessor<AmqpReceiveLink, Message> implements Subscription {
    private static final ClientLogger LOGGER = new ClientLogger(AmqpReceiveLinkProcessor.class);
    private final Object lock = new Object();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final Deque<Message> messageQueue = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean linkHasNoCredits = new AtomicBoolean();
    private final Object creditsAdded = new Object();

    private final AtomicReference<CoreSubscriber<? super Message>> downstream = new AtomicReference<>();
    private final AtomicInteger wip = new AtomicInteger();

    private final int prefetch;
    private final String entityPath;
    private final Disposable connectionProcessor;
    private final int maxQueueSize;

    private volatile Throwable lastError;
    private volatile boolean isCancelled;
    private volatile AmqpReceiveLink currentLink;
    private volatile String currentLinkName;
    private volatile Disposable currentLinkSubscriptions;

    // Opting to use AtomicReferenceFieldUpdater because Project Reactor provides utility methods that calculates
    // backpressure requests, sets the upstream correctly, and reports its state.
    private volatile Subscription upstream;
    private static final AtomicReferenceFieldUpdater<AmqpReceiveLinkProcessor, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(AmqpReceiveLinkProcessor.class, Subscription.class,
            "upstream");

    /**
     * The number of requested messages.
     */
    private volatile long requested;
    private static final AtomicLongFieldUpdater<AmqpReceiveLinkProcessor> REQUESTED =
        AtomicLongFieldUpdater.newUpdater(AmqpReceiveLinkProcessor.class, "requested");

    /**
     * Creates an instance of {@link AmqpReceiveLinkProcessor}.
     *
     * @param prefetch The number if messages to initially fetch.
     * @param connectionProcessor A {@link Disposable} reference to the connection-processor which produces
     *                           the connection on which receive-links are hosted. It is used to eagerly check
     *                           if the connection-processor is disposed so that this link-processor can self dispose.
     *
     * @throws NullPointerException if {@code retryPolicy} is null.
     * @throws IllegalArgumentException if {@code prefetch} is less than 0.
     */
    public AmqpReceiveLinkProcessor(String entityPath, int prefetch, Disposable connectionProcessor) {
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");

        if (prefetch < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'prefetch' cannot be less than 0."));
        }

        this.prefetch = prefetch;
        this.maxQueueSize = prefetch * 2;
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
        LOGGER.info("Setting new subscription for receive link processor");

        if (!Operators.setOnce(UPSTREAM, this, subscription)) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Cannot set upstream twice."));
        }

        requestUpstream();
    }

    @Override
    public int getPrefetch() {
        return prefetch;
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
            LOGGER.atWarning()
                .addKeyValue(LINK_NAME_KEY, next.getLinkName())
                .addKeyValue(ENTITY_PATH_KEY, next.getEntityPath())
                .log("Got another link when we have already terminated processor.");
            Operators.onNextDropped(next, currentContext());
            return;
        }

        final String linkName = next.getLinkName();

        LOGGER.atInfo()
            .addKeyValue(LINK_NAME_KEY, linkName)
            .addKeyValue(ENTITY_PATH_KEY, next.getEntityPath())
            .log("Setting next AMQP receive link.");

        final AmqpReceiveLink oldChannel;
        final Disposable oldSubscription;
        synchronized (lock) {
            oldChannel = currentLink;
            oldSubscription = currentLinkSubscriptions;

            currentLink = next;
            currentLinkName = next.getLinkName();

            // Empty credit listener is invoked when there are no credits left on the underlying link.
            next.setEmptyCreditListener(() -> {
                final int credits;
                synchronized (creditsAdded) {
                    credits = getCreditsToAdd();

                    // This means that considering the downstream request and current size of the message queue, we
                    // have enough messages to satisfy them.
                    // Thus, there are no credits on the link AND we are not going to add anymore.
                    // We'll wait until the next time downstream calls request(long) to get more events.
                    if (credits < 1) {
                        linkHasNoCredits.compareAndSet(false, true);
                    } else {
                        LOGGER.atInfo()
                            .addKeyValue(LINK_NAME_KEY, linkName)
                            .addKeyValue(ENTITY_PATH_KEY, next.getEntityPath())
                            .addKeyValue(CREDITS_KEY, credits)
                            .log("Link is empty. Adding more credits.");
                    }
                }

                return credits;
            });

            currentLinkSubscriptions = Disposables.composite(
                // For a new link, add the prefetch as credits.
                next.getEndpointStates().filter(e -> e == AmqpEndpointState.ACTIVE).next()
                    .flatMap(state -> {
                        // If there was already a subscriber downstream who made a request, see if that is more than
                        // the prefetch. If it is, then add the difference. (ie. if they requested 500, but our
                        // prefetch is 100, we'll add 500 credits rather than 100.
                        final Mono<Void> operation;
                        synchronized (creditsAdded) {
                            final int creditsToAdd = getCreditsToAdd();
                            final int total = Math.max(prefetch, creditsToAdd);

                            LOGGER.atVerbose()
                                .addKeyValue(LINK_NAME_KEY, linkName)
                                .addKeyValue("prefetch", prefetch)
                                .addKeyValue(CREDITS_KEY, creditsToAdd)
                                .log("Adding initial credits.");

                            operation = next.addCredits(total);
                        }

                        return operation;
                    })
                    .subscribe(noop -> {
                    }, error -> LOGGER.atInfo().addKeyValue(LINK_NAME_KEY, linkName).log("Link was already closed. Could not add credits.")),
                next.getEndpointStates().subscribeOn(Schedulers.boundedElastic()).subscribe(
                    state -> {
                        // Connection was successfully opened, we can reset the retry interval.
                        if (state == AmqpEndpointState.ACTIVE) {
                            LOGGER.atInfo()
                                .addKeyValue(LINK_NAME_KEY, linkName)
                                .addKeyValue(CREDITS_KEY, next.getCredits())
                                .log("Link is active.");
                            retryAttempts.set(0);
                        }
                    },
                    error -> {
                        if (error instanceof AmqpException) {
                            AmqpException amqpException = (AmqpException) error;
                            if (amqpException.getErrorCondition() == AmqpErrorCondition.LINK_STOLEN
                                && amqpException.getContext() != null
                                && amqpException.getContext() instanceof LinkErrorContext) {
                                LinkErrorContext errorContext = (LinkErrorContext) amqpException.getContext();
                                if (currentLink != null
                                    && !currentLink.getLinkName().equals(errorContext.getTrackingId())) {
                                    LOGGER.atInfo()
                                        .addKeyValue(LINK_NAME_KEY, linkName)
                                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                                        .addKeyValue(TRACKING_ID_KEY, errorContext.getTrackingId())
                                        .log("Link lost signal received for a link that is not current. Ignoring the error.");
                                    return;
                                }
                            }
                        }

                        currentLink = null;
                        onError(error);
                    },
                    () -> {
                        if (connectionProcessor.isDisposed() || isTerminated()
                            || UPSTREAM.get(this) == Operators.cancelledSubscription()) {

                            LOGGER.atInfo()
                                .addKeyValue(LINK_NAME_KEY, linkName)
                                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                                .log("Terminal state reached. Disposing of link processor.");

                            dispose();
                        } else {
                            LOGGER.atInfo()
                                .addKeyValue(LINK_NAME_KEY, linkName)
                                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                                .log("Receive link endpoint states are closed. Requesting another.");

                            final AmqpReceiveLink existing = currentLink;
                            currentLink = null;
                            currentLinkName = null;

                            disposeReceiver(existing);
                            requestUpstream();
                        }
                    }),
                next.receive()
                    .onBackpressureBuffer(maxQueueSize, BufferOverflowStrategy.ERROR)
                    .subscribe(
                        message -> {
                            messageQueue.add(message);
                            drain();
                        },
                        error -> {
                            // If the receive() on upstream ReactorReceiver (the var 'next' here) terminates with an
                            // error, the ReceiveLinkProcessor (RLP) will not propagate the error to the downstream
                            // subscriber; for the downstream subscriber, an error is a terminal event.
                            // The purpose of RLP is to recover transparently.
                            // Given the ReactorReceiver completes the getEndpointStates() Flux upon it's termination,
                            // the RLP will recover by obtaining a new ReactorReceiver; this way, the downstream
                            // continues to be served using later ReactorReceiver rather than terminating from
                            // intermediate errors.
                            //
                            LOGGER.atVerbose()
                                .addKeyValue(LINK_NAME_KEY, linkName)
                                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                                .log("Receiver is terminated.", error);
                        }));
        }

        disposeReceiver(oldChannel);

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

        final boolean terminateSubscriber = isTerminated()
            || (currentLink == null && upstream == Operators.cancelledSubscription());
        if (isTerminated()) {
            LOGGER.atInfo()
                .addKeyValue(LINK_NAME_KEY, currentLinkName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log("AmqpReceiveLink is already terminated.");
        } else if (currentLink == null && upstream == Operators.cancelledSubscription()) {
            LOGGER.info("There is no current link and upstream is terminated.");
        }

        if (terminateSubscriber) {
            actual.onSubscribe(Operators.emptySubscription());
            if (hasError()) {
                actual.onError(lastError);
            } else {
                actual.onComplete();
            }

            return;
        }

        if (downstream.compareAndSet(null, actual)) {
            actual.onSubscribe(this);
            drain();
        } else {
            Operators.error(actual, LOGGER.logExceptionAsError(new IllegalStateException(
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

        LOGGER.atInfo()
            .addKeyValue(LINK_NAME_KEY, currentLinkName)
            .log("Error on receive link.", throwable);

        if (isTerminated() || isCancelled) {
            LOGGER.atInfo()
                .addKeyValue(LINK_NAME_KEY, currentLinkName)
                .log("AmqpReceiveLinkProcessor is terminated. Cannot process another error.", throwable);
            Operators.onErrorDropped(throwable, currentContext());
            return;
        }

        if (connectionProcessor.isDisposed()) {
            LOGGER.atInfo()
                .addKeyValue(LINK_NAME_KEY, currentLinkName)
                .log("Parent connection is disposed. Not reopening on error.");
        }

        lastError = throwable;
        isTerminated.set(true);

        final CoreSubscriber<? super Message> subscriber = downstream.get();
        if (subscriber != null) {
            subscriber.onError(throwable);
        }

        onDispose();
    }

    /**
     * When the upstream publisher has no more items to emit.
     */
    @Override
    public void onComplete() {
        LOGGER.atInfo()
            .addKeyValue(LINK_NAME_KEY, currentLinkName)
            .log("Receive link completed from upstream.");

        UPSTREAM.set(this, Operators.cancelledSubscription());
    }

    @Override
    public void dispose() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        LOGGER.atInfo()
            .addKeyValue(LINK_NAME_KEY, currentLinkName)
            .log("Disposing receive link.");

        drain();
        onDispose();
    }

    /**
     * When downstream subscriber makes a back-pressure request.
     */
    @Override
    public void request(long request) {
        if (!Operators.validate(request)) {
            LOGGER.warning("Invalid request: {}", request);
            return;
        }

        Operators.addCap(REQUESTED, this, request);

        addCreditsToLink("Backpressure request from downstream. Request: " + request);
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

    /**
     * Requests another receive link from upstream.
     */
    private void requestUpstream() {
        if (isTerminated()) {
            LOGGER.info("Processor is terminated. Not requesting another link.");
            return;
        } else if (UPSTREAM.get(this) == null) {
            LOGGER.info("There is no upstream. Not requesting another link.");
            return;
        } else if (UPSTREAM.get(this) == Operators.cancelledSubscription()) {
            LOGGER.info("Upstream is cancelled or complete. Not requesting another link.");
            return;
        }

        synchronized (lock) {
            if (currentLink != null) {
                LOGGER.info("Current link exists. Not requesting another link.");
                return;
            }
        }

        LOGGER.info("Requesting a new AmqpReceiveLink from upstream.");
        UPSTREAM.get(this).request(1L);
    }

    private void onDispose() {
        disposeReceiver(currentLink);

        currentLink = null;
        currentLinkName = null;

        if (currentLinkSubscriptions != null) {
            currentLinkSubscriptions.dispose();
        }

        Operators.onDiscardQueueWithClear(messageQueue, currentContext(), null);
    }

    private void drain() {
        // If there are multiple threads that enter this, they'll have incremented the wip number, and we'll know
        // how many were 'missed'.
        if (wip.getAndIncrement() != 0) {
            return;
        }

        int missed = 1;

        while (missed != 0) {
            drainQueue();

            // If there are multiple threads that tried to enter this, we would have missed some, so we'll go back
            // through the loop until we have not missed any other work.
            missed = wip.addAndGet(-missed);
        }
    }

    private void drainQueue() {
        final CoreSubscriber<? super Message> subscriber = downstream.get();
        if (subscriber == null || checkAndSetTerminated()) {
            return;
        }

        long numberRequested = REQUESTED.get(this);
        boolean isEmpty = messageQueue.isEmpty();
        while (numberRequested != 0L && !isEmpty) {
            if (checkAndSetTerminated()) {
                break;
            }

            long numberEmitted = 0L;
            while (numberRequested != numberEmitted) {
                if (isEmpty && checkAndSetTerminated()) {
                    break;
                }

                Message message = messageQueue.poll();
                if (message == null) {
                    break;
                }

                if (isCancelled) {
                    Operators.onDiscard(message, subscriber.currentContext());
                    Operators.onDiscardQueueWithClear(messageQueue, subscriber.currentContext(), null);
                    return;
                }

                try {
                    subscriber.onNext(message);
                } catch (Exception e) {
                    LOGGER.atError()
                        .addKeyValue(LINK_NAME_KEY, currentLinkName)
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .log("Exception occurred while handling downstream onNext operation.", e);

                    throw LOGGER.logExceptionAsError(Exceptions.propagate(
                        Operators.onOperatorError(upstream, e, message, subscriber.currentContext())));
                }

                numberEmitted++;
                isEmpty = messageQueue.isEmpty();
            }

            final long requestedMessages = REQUESTED.get(this);
            if (requestedMessages != Long.MAX_VALUE) {
                numberRequested = REQUESTED.addAndGet(this, -numberEmitted);
            }
        }

        // In the drain loop, add credits only when the queue is empty, link has no credits and there are still
        // outstanding requests to be fulfilled.
        AmqpReceiveLink link = this.currentLink;
        if (numberRequested > 0L && isEmpty && link != null && link.getCredits() <= 0) {
            addCreditsToLink("Adding more credits in drain loop.");
        }
    }

    private boolean checkAndSetTerminated() {
        if (!isTerminated()) {
            return false;
        }

        final CoreSubscriber<? super Message> subscriber = downstream.get();
        final Throwable error = lastError;
        if (error != null) {
            subscriber.onError(error);
        } else {
            subscriber.onComplete();
        }

        disposeReceiver(currentLink);

        messageQueue.clear();
        return true;
    }

    /**
     * Consolidates all credits calculation when checking to see if more should be added. This is invoked in
     * {@link #drainQueue()} and {@link #request(long)}.
     *
     * Calculates if there are enough credits to satisfy the downstream subscriber. If there is not AND the link has no
     * more credits, we will add them onto the link.
     *
     * In the case that the link has some credits, but _not_ enough to satisfy the request, when the link is empty, it
     * will call {@link AmqpReceiveLink#setEmptyCreditListener(Supplier)} to get how much is remaining.
     *
     * @param message Additional message for context.
     */
    private void addCreditsToLink(String message) {
        synchronized (creditsAdded) {
            final AmqpReceiveLink link = currentLink;
            final int credits = getCreditsToAdd();

            if (link == null) {

                LOGGER.atVerbose()
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue(CREDITS_KEY, credits)
                    .log("There is no link to add credits to.");

                return;
            }

            final String linkName = link.getLinkName();

            if (credits < 1) {
                LOGGER.atVerbose()
                    .addKeyValue(LINK_NAME_KEY, linkName)
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue(CREDITS_KEY, credits)
                    .log("There are no additional credits to add.");

                return;
            }

            int currentLinkCredits = link.getCredits();
            // Add more credits if the link has fewer credits than prefetch value. This allows users to control how
            // many events to buffer on the client and also control the throughput. If users need higher throughput,
            // they can set a higher prefetch number and allocate larger heap size accordingly.
            if (currentLinkCredits < prefetch) {
                LOGGER.atInfo()
                    .addKeyValue(LINK_NAME_KEY, linkName)
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue(CREDITS_KEY, credits)
                    .addKeyValue("message", message)
                    .log("Link running low on credits. Adding more.");
                link.addCredits(credits).subscribe(noop -> {
                }, error -> {
                    LOGGER.atInfo()
                        .addKeyValue(LINK_NAME_KEY, linkName)
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .log("Link was already closed. Could not add credits.");

                    linkHasNoCredits.compareAndSet(false, true);
                });
            }
        }
    }

    /**
     * Gets the number of credits to add based on {@link #requested} and how many messages are still in queue.
     * If {@link #requested} is {@link Long#MAX_VALUE}, which indicates no-backpressure,
     * then we use the {@link #prefetch} value as credit.
     *
     * @return The number of credits to add.
     */
    private int getCreditsToAdd() {
        final CoreSubscriber<? super Message> subscriber = downstream.get();
        final long request = REQUESTED.get(this);

        final int credits;
        if (subscriber == null || request == 0) {
            credits = 0;
        } else if (request == Long.MAX_VALUE) {
            credits = prefetch;
        } else {
            final int remaining = Long.valueOf(request).intValue() - messageQueue.size();
            credits = Math.max(remaining, 0);
        }

        return credits;
    }

    private void disposeReceiver(AmqpReceiveLink link) {
        if (link == null) {
            return;
        }

        try {
            ((AsyncCloseable) link).closeAsync().subscribe();
        } catch (Exception error) {
            LOGGER.atWarning()
                .addKeyValue(LINK_NAME_KEY, link.getLinkName())
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log("Unable to dispose of link.", error);
        }
    }
}
