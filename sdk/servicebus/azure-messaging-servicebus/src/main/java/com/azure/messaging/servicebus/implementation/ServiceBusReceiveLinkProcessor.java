// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.util.AsyncCloseable;
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
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DELIVERY_STATE_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.LOCK_TOKEN_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.NUMBER_OF_REQUESTED_MESSAGES_KEY;

/**
 * Processes AMQP receive links into a stream of AMQP messages.
 *
 * This is almost a carbon copy of AmqpReceiveLinkProcessor. When we can abstract it from proton-j, it would be nice to
 * unify this.
 */
public class ServiceBusReceiveLinkProcessor extends FluxProcessor<ServiceBusReceiveLink, Message>
    implements Subscription {
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiveLinkProcessor.class);
    private final Object lock = new Object();
    private final Object queueLock = new Object();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final AtomicReference<String> linkName = new AtomicReference<>();

    // Queue containing all the prefetched messages.
    private final Deque<Message> messageQueue = new ConcurrentLinkedDeque<>();
    // size() on Deque is O(n) operation, so we use an integer to keep track. All reads and writes to this are gated by
    // the `queueLock`.
    private final AtomicInteger pendingMessages = new AtomicInteger();
    private final int minimumNumberOfMessages;
    private final int prefetch;

    private final AtomicReference<CoreSubscriber<? super Message>> downstream = new AtomicReference<>();
    private final AtomicInteger wip = new AtomicInteger();

    private final AmqpRetryPolicy retryPolicy;

    private volatile Throwable lastError;
    private volatile boolean isCancelled;
    private volatile ServiceBusReceiveLink currentLink;
    private volatile Disposable currentLinkSubscriptions;
    private volatile Disposable retrySubscription;

    // Opting to use AtomicReferenceFieldUpdater because Project Reactor provides utility methods that calculates
    // backpressure requests, sets the upstream correctly, and reports its state.
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
     *
     * @throws NullPointerException if {@code retryPolicy} is null.
     * @throws IllegalArgumentException if {@code prefetch} is less than 0.
     */
    public ServiceBusReceiveLinkProcessor(int prefetch, AmqpRetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");

        if (prefetch < 0) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'prefetch' cannot be less than 0."));
        }

        this.prefetch = prefetch;

        // When the queue has this number of messages left, it's time to add more credits to refill the prefetch queue.
        this.minimumNumberOfMessages = Math.floorDiv(prefetch, 3);
    }

    public String getLinkName() {
        return linkName.get();
    }

    public Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
        if (isDisposed()) {
            return monoError(logger.atError()
                    .addKeyValue(LOCK_TOKEN_KEY, lockToken)
                    .addKeyValue(DELIVERY_STATE_KEY, deliveryState),
                new IllegalStateException(String.format(
                    "lockToken[%s]. state[%s]. Cannot update disposition on closed processor.", lockToken, deliveryState)));
        }

        final ServiceBusReceiveLink link = currentLink;
        if (link == null) {
            return monoError(logger.atError()
                    .addKeyValue(LOCK_TOKEN_KEY, lockToken)
                    .addKeyValue(DELIVERY_STATE_KEY, deliveryState),
                new IllegalStateException(String.format(
                    "lockToken[%s]. state[%s]. Cannot update disposition with no link.", lockToken, deliveryState)));
        }

        return link.updateDisposition(lockToken, deliveryState).onErrorResume(error -> {
            if (error instanceof AmqpException) {
                AmqpException amqpException = (AmqpException) error;
                if (AmqpErrorCondition.TIMEOUT_ERROR.equals(amqpException.getErrorCondition())) {
                    return link.closeAsync().then(Mono.error(error));
                }
            }
            return Mono.error(error);
        });
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
    public void onNext(ServiceBusReceiveLink next) {
        Objects.requireNonNull(next, "'next' cannot be null.");

        if (isTerminated()) {
            logger.atWarning()
                .addKeyValue(LINK_NAME_KEY, next.getLinkName())
                .addKeyValue(ENTITY_PATH_KEY, next.getEntityPath())
                .log("Got another link when we have already terminated processor.");

            Operators.onNextDropped(next, currentContext());
            return;
        }

        final String linkName = next.getLinkName();
        final String entityPath = next.getEntityPath();

        logger.atInfo()
            .addKeyValue(LINK_NAME_KEY, linkName)
            .addKeyValue(ENTITY_PATH_KEY, entityPath)
            .log("Setting next AMQP receive link.");

        final AmqpReceiveLink oldChannel;
        final Disposable oldSubscription;
        synchronized (lock) {
            oldChannel = currentLink;
            oldSubscription = currentLinkSubscriptions;

            currentLink = next;
            next.setEmptyCreditListener(() -> 0);

            currentLinkSubscriptions = Disposables.composite(
                next.receive().publishOn(Schedulers.boundedElastic()).subscribe(
                    message -> {
                        synchronized (queueLock) {
                            messageQueue.add(message);
                            pendingMessages.incrementAndGet();
                        }

                        drain();
                    },
                    error -> {
                        // When the receive on AmqpReceiveLink (e.g., ServiceBusReactorReceiver) terminates
                        // with an error, we expect the recovery to happen in response to the terminal events
                        // in link EndpointState Flux.
                        logger.atVerbose()
                            .addKeyValue(LINK_NAME_KEY, linkName)
                            .addKeyValue(ENTITY_PATH_KEY, entityPath)
                            .log("Receiver is terminated.", error);
                    }),
                next.getEndpointStates().subscribeOn(Schedulers.boundedElastic()).subscribe(
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
                        if (isTerminated()) {
                            logger.info("Processor is terminated. Disposing of link processor.");
                            dispose();
                        } else if (upstream == Operators.cancelledSubscription()) {
                            logger.info("Upstream has completed. Disposing of link processor.");
                            dispose();
                        } else {
                            logger.info("Receive link endpoint states are closed. Requesting another.");
                            final AmqpReceiveLink existing = currentLink;
                            currentLink = null;


                            disposeReceiver(existing);
                            requestUpstream();
                        }
                    }));
        }

        checkAndAddCredits(next);
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
            final AmqpReceiveLink link = currentLink;
            final String linkName = link != null ? link.getLinkName() : "n/a";
            final String entityPath = link != null ? link.getEntityPath() : "n/a";

            logger.atInfo()
                .addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log("AmqpReceiveLink is already terminated.");
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

        if (downstream.compareAndSet(null, actual)) {
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

        final AmqpReceiveLink link = currentLink;
        final String linkName = link != null ? link.getLinkName() : "n/a";
        final String entityPath = link != null ? link.getEntityPath() : "n/a";

        if (retryInterval != null && upstream != Operators.cancelledSubscription()) {
            logger.atWarning()
                .addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue("attempt", attempt)
                .addKeyValue("retryAfter", retryInterval.toMillis())
                .log("Transient error occurred.", throwable);

            retrySubscription = Mono.delay(retryInterval).subscribe(i -> requestUpstream());

            return;
        }

        logger.atWarning()
            .addKeyValue(LINK_NAME_KEY, linkName)
            .addKeyValue(ENTITY_PATH_KEY, entityPath)
            .log("Non-retryable error occurred in AMQP receive link.", throwable);

        lastError = throwable;

        isTerminated.set(true);

        final CoreSubscriber<? super Message> subscriber = downstream.get();
        if (subscriber != null) {
            subscriber.onError(throwable);
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
            logger.atWarning()
                .addKeyValue(NUMBER_OF_REQUESTED_MESSAGES_KEY, request)
                .log("Invalid request");
            return;
        }

        Operators.addCap(REQUESTED, this, request);

        final AmqpReceiveLink link = currentLink;
        if (link == null) {
            return;
        }

        checkAndAddCredits(link);
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

        disposeReceiver(currentLink);

        currentLink = null;

        if (currentLinkSubscriptions != null) {
            currentLinkSubscriptions.dispose();
        }
    }

    private void drain() {
        if (wip.getAndIncrement() != 0) {
            return;
        }

        int missed = 1;
        while (missed != 0) {
            drainQueue();
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

                final Message message = messageQueue.poll();
                if (message == null) {
                    break;
                }

                if (isCancelled) {
                    Operators.onDiscard(message, subscriber.currentContext());

                    synchronized (queueLock) {
                        Operators.onDiscardQueueWithClear(messageQueue, subscriber.currentContext(), null);
                        pendingMessages.set(0);
                    }

                    return;
                }

                try {
                    subscriber.onNext(message);

                    // RECEIVE_DELETE Mode: No need to settle message because they're automatically settled by the link.
                    // PEEK_LOCK Mode: Consider message processed, as `onNext` is complete, So decrement the count.
                    pendingMessages.decrementAndGet();

                    if (prefetch > 0) { // re-fill messageQueue if there is prefetch configured.
                        checkAndAddCredits(currentLink);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred while handling downstream onNext operation.", e);
                    throw logger.logExceptionAsError(Exceptions.propagate(
                        Operators.onOperatorError(upstream, e, message, subscriber.currentContext())));
                }

                numberEmitted++;
                isEmpty = messageQueue.isEmpty();
            }

            if (REQUESTED.get(this) != Long.MAX_VALUE) {
                numberRequested = REQUESTED.addAndGet(this, -numberEmitted);
            }
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

        synchronized (queueLock) {
            messageQueue.clear();
            pendingMessages.set(0);
        }

        return true;
    }

    private void checkAndAddCredits(AmqpReceiveLink link) {
        if (link == null) {
            return;
        }

        synchronized (lock) {
            final int linkCredits = link.getCredits();
            final int credits = getCreditsToAdd(linkCredits);
            if (credits > 0) {
                link.addCredits(credits).subscribe();
            }
        }
    }

    private int getCreditsToAdd(int linkCredits) {
        final CoreSubscriber<? super Message> subscriber = downstream.get();
        final long r = REQUESTED.get(this);
        final boolean hasBackpressure = r != Long.MAX_VALUE;

        if (subscriber == null || r == 0) {
            logger.info("Not adding credits. No downstream subscribers or items requested.");
            return 0;
        }

        final int creditsToAdd;
        final int expectedTotalCredit;
        if (prefetch == 0) {
            if (r <= Integer.MAX_VALUE) {
                expectedTotalCredit = (int) r;
            } else {
                //This won't really happen in reality.
                //For async client, receiveMessages() calls "return receiveMessagesNoBackPressure().limitRate(1, 0);".
                //So it will request one by one from this link processor, even though the user's request has no
                //back pressure.
                //For sync client, the sync subscriber has back pressure.
                //The request count uses the argument of method receiveMessages(int maxMessages).
                //It's at most Integer.MAX_VALUE.
                expectedTotalCredit = Integer.MAX_VALUE;
            }
        } else {
            expectedTotalCredit = prefetch;
        }

        synchronized (queueLock) {
            final int queuedMessages = pendingMessages.get();
            final int pending = queuedMessages + linkCredits;

            if (hasBackpressure) {
                creditsToAdd = Math.max(expectedTotalCredit - pending, 0);
            } else {
                // If the queue has less than 1/3 of the prefetch, then add the difference to keep the queue full.
                creditsToAdd = minimumNumberOfMessages >= queuedMessages
                    ? Math.max(expectedTotalCredit - pending, 0)
                    : 0;
            }

            logger.atInfo()
                .addKeyValue("prefetch", getPrefetch())
                .addKeyValue(NUMBER_OF_REQUESTED_MESSAGES_KEY, r)
                .addKeyValue("linkCredits", linkCredits)
                .addKeyValue("expectedTotalCredit", expectedTotalCredit)
                .addKeyValue("queuedMessages", queuedMessages)
                .addKeyValue("creditsToAdd", creditsToAdd)
                .addKeyValue("messageQueueSize", messageQueue.size())
                .log("Adding credits.");
        }

        return creditsToAdd;
    }

    private void disposeReceiver(AmqpReceiveLink link) {
        if (link == null) {
            return;
        }

        try {
            ((AsyncCloseable) link).closeAsync().subscribe();
        } catch (Exception error) {
            logger.atWarning()
                .addKeyValue(LINK_NAME_KEY, link.getLinkName())
                .addKeyValue(ENTITY_PATH_KEY, link.getEntityPath())
                .log("Unable to dispose of link.", error);
        }
    }
}
