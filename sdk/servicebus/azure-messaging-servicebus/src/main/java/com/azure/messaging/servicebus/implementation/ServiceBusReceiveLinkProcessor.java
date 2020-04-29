// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Processes AMQP receive links into a stream of AMQP messages.
 *
 * This is almost a carbon copy of AmqpReceiveLinkProcessor. When we can abstract it from proton-j, it would be nice to
 * unify this.
 */
public class ServiceBusReceiveLinkProcessor extends FluxProcessor<AmqpReceiveLink, Message> implements Subscription {
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiveLinkProcessor.class);
    private final Object lock = new Object();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final Deque<Message> messageQueue = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean hasFirstLink = new AtomicBoolean();
    private final AtomicBoolean linkCreditsAdded = new AtomicBoolean();

    private final int prefetch;
    private final AmqpRetryPolicy retryPolicy;
    private final Disposable parentConnection;
    private final AmqpErrorContext errorContext;

    private volatile Throwable lastError;
    private volatile boolean isCancelled;
    private volatile AmqpReceiveLink currentLink;
    private volatile Disposable currentLinkSubscriptions;
    private volatile Disposable retrySubscription;

    private volatile CoreSubscriber<? super Message> downstream;
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<ServiceBusReceiveLinkProcessor, CoreSubscriber> DOWNSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(ServiceBusReceiveLinkProcessor.class, CoreSubscriber.class,
            "downstream");

    private volatile int wip;
    private static final AtomicIntegerFieldUpdater<ServiceBusReceiveLinkProcessor> WIP =
        AtomicIntegerFieldUpdater.newUpdater(ServiceBusReceiveLinkProcessor.class, "wip");

    private volatile long requested;
    private static final AtomicLongFieldUpdater<ServiceBusReceiveLinkProcessor> REQUESTED =
        AtomicLongFieldUpdater.newUpdater(ServiceBusReceiveLinkProcessor.class, "requested");

    private volatile Subscription upstream;
    private static final AtomicReferenceFieldUpdater<ServiceBusReceiveLinkProcessor, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(ServiceBusReceiveLinkProcessor.class, Subscription.class,
            "upstream");

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

    public Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
        if (isDisposed()) {
            return monoError(logger, new IllegalStateException(String.format(
                "lockToken[%s]. state[%s]. Cannot update disposition on closed processor.", lockToken, deliveryState)));
        }

        final AmqpReceiveLink link = currentLink;
        if (link == null) {
            return monoError(logger, new IllegalStateException(String.format(
                "lockToken[%s]. state[%s]. Cannot update disposition with no link.", lockToken, deliveryState)));
        } else if (!(link instanceof ServiceBusReactorReceiver)) {
            return monoError(logger, new IllegalStateException(String.format(
                "lockToken[%s]. state[%s]. Cannot update disposition with non Service Bus receive link.",
                lockToken, deliveryState)));
        }

        return ((ServiceBusReactorReceiver) link).updateDisposition(lockToken, deliveryState);
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

    @Override
    public int getPrefetch() {
        return prefetch;
    }

    /**
     * When a subscription is obtained from upstream publisher.
     *
     * @param subscription Subscription to upstream publisher.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        Objects.requireNonNull(subscription, "'subscription' cannot be null");

        if (!Operators.setOnce(UPSTREAM, this, subscription)) {
            throw logger.logExceptionAsError(new IllegalStateException("Cannot set upstream twice."));
        }

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
            Operators.onNextDropped(next, currentContext());
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

            if (!hasFirstLink.getAndSet(true)) {
                linkCreditsAdded.set(true);
                next.addCredits(prefetch);
            }

            next.setEmptyCreditListener(() -> getCreditsToAdd());

            currentLinkSubscriptions = Disposables.composite(
                next.receive().publishOn(Schedulers.boundedElastic()).subscribe(message -> {
                    messageQueue.add(message);
                    drain();
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
                            downstream.onComplete();
                        } else if (isTerminated()) {
                            logger.info("Processor is disposed.");
                            downstream.onComplete();
                        } else if (upstream == Operators.cancelledSubscription()) {
                            logger.info("Upstream has no more items to emit. Disposing.");
                            dispose();
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
            final AmqpReceiveLink link = currentLink;
            final String linkName = link != null ? link.getLinkName() : "n/a";
            final String entityPath = link != null ? link.getEntityPath() : "n/a";

            logger.info("linkName[{}] entityPath[{}]. AmqpReceiveLink is already terminated.", linkName, entityPath);
        } else if (currentLink == null && upstream == Operators.cancelledSubscription()) {
            logger.info("There is no current link and upstream is terminated.");
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

        if (DOWNSTREAM.compareAndSet(this, null, actual)) {
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

        if (isTerminated()) {
            logger.info("AmqpReceiveLinkProcessor is terminated. Not reopening on error.");
            return;
        }

        final int attempt = retryAttempts.incrementAndGet();
        final Duration retryInterval = retryPolicy.calculateRetryDelay(throwable, attempt);

        if (retryInterval != null && !parentConnection.isDisposed() && upstream != Operators.cancelledSubscription()) {
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
     * When upstream has completed emitting messages.
     */
    @Override
    public void onComplete() {
        this.upstream = Operators.cancelledSubscription();
    }

    /**
     * When downstream subscriber makes a back-pressure request.
     */
    @Override
    public void request(long request) {
        if (!Operators.validate(request)) {
            logger.warning("Invalid request: {}", request);
            return;
        }

        Operators.addCap(REQUESTED, this, request);

        final AmqpReceiveLink link = currentLink;
        if (link != null && !linkCreditsAdded.getAndSet(true)) {
            int credits = getCreditsToAdd();
            logger.info("Link credits not yet added. Adding: {}", credits);
            link.addCredits(credits);
        }

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
            logger.info("Processor is terminated. Not requesting another link.");
            return;
        } else if (upstream == null) {
            logger.info("There is no upstream. Not requesting another link.");
            return;
        } else if (upstream == Operators.cancelledSubscription()) {
            logger.info("Upstream is cancelled or complete. Not requesting another link.");
            return;
        }

        synchronized (lock) {
            if (currentLink != null) {
                logger.info("Current link exists. Not requesting another link.");
                return;
            }
        }

        logger.info("Requesting a new AmqpReceiveLink from upstream.");
        upstream.request(1L);
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
        if (downstream == null || checkAndSetTerminated()) {
            return;
        }

        long numberRequested = requested;
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
                    Operators.onDiscard(message, downstream.currentContext());
                    Operators.onDiscardQueueWithClear(messageQueue, downstream.currentContext(), null);
                    return;
                }

                try {
                    downstream.onNext(message);
                } catch (Exception e) {
                    logger.error("Exception occurred while handling downstream onNext operation.", e);
                    throw logger.logExceptionAsError(Exceptions.propagate(
                        Operators.onOperatorError(upstream, e, message, downstream.currentContext())));
                }

                numberEmitted++;
                isEmpty = messageQueue.isEmpty();
            }

            if (requested != Long.MAX_VALUE) {
                numberRequested = REQUESTED.addAndGet(this, -numberEmitted);
            }
        }
    }

    private boolean checkAndSetTerminated() {
        if (!isTerminated()) {
            return false;
        }

        final Throwable error = lastError;
        if (error != null) {
            downstream.onError(error);
        } else {
            downstream.onComplete();
        }

        if (currentLink != null) {
            currentLink.dispose();
        }

        messageQueue.clear();
        return true;
    }

    private int getCreditsToAdd() {
        final CoreSubscriber<? super Message> d = downstream;
        final long r = requested;
        if (d == null || r == 0) {
            logger.info("Not adding credits. No downstream subscribers or items requested.");
            linkCreditsAdded.set(false);
            return 0;
        }

        linkCreditsAdded.set(true);

        // If there is no back pressure, always add 1. Otherwise, add whatever is requested.
        return r == Long.MAX_VALUE ? 1 : Long.valueOf(r).intValue();
    }
}
