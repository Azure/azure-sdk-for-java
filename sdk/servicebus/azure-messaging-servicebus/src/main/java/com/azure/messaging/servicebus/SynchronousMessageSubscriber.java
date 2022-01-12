// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Operators;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Subscriber that listens to events and publishes them downstream and publishes events to them in the order received.
 */
class SynchronousMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessage> {
    private final ClientLogger logger = new ClientLogger(SynchronousMessageSubscriber.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicInteger wip = new AtomicInteger();
    private final ConcurrentLinkedQueue<SynchronousReceiveWork> workQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedDeque<ServiceBusReceivedMessage> bufferMessages = new ConcurrentLinkedDeque<>();

    private final Object currentWorkLock = new Object();
    private volatile SynchronousReceiveWork currentWork;

    /**
     * The number of requested messages.
     */
    private volatile long requested;
    private static final AtomicLongFieldUpdater<SynchronousMessageSubscriber> REQUESTED =
        AtomicLongFieldUpdater.newUpdater(SynchronousMessageSubscriber.class, "requested");

    private volatile Subscription upstream;
    private static final AtomicReferenceFieldUpdater<SynchronousMessageSubscriber, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(SynchronousMessageSubscriber.class, Subscription.class,
            "upstream");

    /**
     * Creates a synchronous subscriber with some initial work to queue.
     *
     * @param initialWork Initial work to queue.
     *
     * @throws NullPointerException if {@code initialWork} is null.
     * @throws IllegalArgumentException if {@code initialWork.getNumberOfEvents()} is less than 1.
     */
    SynchronousMessageSubscriber(SynchronousReceiveWork initialWork) {
        this.workQueue.add(Objects.requireNonNull(initialWork, "'initialWork' cannot be null."));

        if (initialWork.getNumberOfEvents() < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'numberOfEvents' cannot be less than 1. Actual: " + initialWork.getNumberOfEvents()));
        }

        Operators.addCap(REQUESTED, this, initialWork.getNumberOfEvents());
    }

    /**
     * On an initial subscription, will take the first work item, and request that amount of work for it.
     *
     * @param subscription Subscription for upstream.
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        if (!Operators.setOnce(UPSTREAM, this, subscription)) {
            logger.warning("This should only be subscribed to once. Ignoring subscription.");
            return;
        }

        // Initialises or returns existing work. If existing work is returned, it's a no-op. Otherwise, it'll "start"
        // the new current work.
        getOrUpdateCurrentWork();

        subscription.request(REQUESTED.get(this));
    }

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will dispose of
     * the subscriber.
     *
     * @param message Event to publish.
     */
    @Override
    protected void hookOnNext(ServiceBusReceivedMessage message) {
        bufferMessages.add(message);
        drain();
    }

    /**
     * Queue the work to be picked up by drain loop.
     *
     * @param work to be queued.
     */
    void queueWork(SynchronousReceiveWork work) {
        Objects.requireNonNull(work, "'work' cannot be null");

        workQueue.add(work);

        // If previous work items were completed, the message queue is empty and currentWork == null. Update the
        // current work and request items upstream if we need to.
        if (workQueue.peek() == work) {
            logger.verbose("workId[{}] numberOfEvents[{}] timeout[{}] First work in queue. Requesting upstream if "
                    + "needed.", work.getId(), work.getNumberOfEvents(), work.getTimeout());

            getOrUpdateCurrentWork();
        } else {
            logger.verbose("workId[{}] numberOfEvents[{}] timeout[{}] Queuing receive work.", work.getId(),
                work.getNumberOfEvents(), work.getTimeout());
        }

        if (UPSTREAM.get(this) != null) {
            drain();
        }
    }

    /**
     * Drain the work, only one thread can be in this loop at a time.
     */
    private void drain() {
        if (wip.getAndIncrement() != 0) {
            return;
        }

        int missed = 1;
        while (missed != 0) {
            try {
                drainQueue();
            } finally {
                missed = wip.addAndGet(-missed);
            }
        }
    }

    /***
     * Drain the queue using a lock on current work in progress.
     */
    private void drainQueue() {
        if (isTerminated()) {
            return;
        }

        long numberRequested = REQUESTED.get(this);
        boolean isEmpty = bufferMessages.isEmpty();

        SynchronousReceiveWork work;
        while (numberRequested != 0L && !isEmpty) {
            if (isTerminated()) {
                break;
            }

            long numberEmitted = 0L;
            while (numberRequested != numberEmitted) {
                if (isEmpty || isTerminated()) {
                    break;
                }

                final ServiceBusReceivedMessage message = bufferMessages.poll();
                boolean isEmitted = false;
                while (!isEmitted) {
                    work = getOrUpdateCurrentWork();
                    if (work == null) {
                        break;
                    }

                    isEmitted = work.emitNext(message);
                }

                // We could not emit the last message that we polled because there were no work items.
                // Push this back to the head of the work queue.
                if (!isEmitted) {
                    bufferMessages.addFirst(message);
                    break;
                }

                numberEmitted++;
                isEmpty = bufferMessages.isEmpty();
            }

            final long requestedMessages = REQUESTED.get(this);
            if (requestedMessages != Long.MAX_VALUE) {
                numberRequested = REQUESTED.addAndGet(this, -numberEmitted);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnError(Throwable throwable) {
        dispose("Errors occurred upstream", throwable);
    }

    @Override
    protected void hookOnCancel() {
        this.dispose();
    }

    private boolean isTerminated() {
        if (UPSTREAM.get(this) == Operators.cancelledSubscription()) {
            return true;
        }

        return isDisposed.get();
    }

    /**
     * Gets the current work item if it is not terminal and cleans up any existing timeout operations.
     *
     * @return Gets or sets the next work item. Null if there are no work items currently.
     */
    private SynchronousReceiveWork getOrUpdateCurrentWork() {
        synchronized (currentWorkLock) {
            // If the current work isn't terminal, then return it. Otherwise, poll for the next item.
            if (currentWork != null && !currentWork.isTerminal()) {
                return currentWork;
            }

            currentWork = workQueue.poll();
            while (currentWork != null) {
                // For the terminal work, subtract the remaining number of messages from our current request
                // count. This is so we don't keep adding credits for work that was expired but we never
                // received messages for.
                if (currentWork.isTerminal()) {
                    REQUESTED.updateAndGet(this, currentRequest -> {
                        final int remainingEvents = currentWork.getRemainingEvents();

                        if (remainingEvents < 1) {
                            return currentRequest;
                        }

                        final long difference = currentRequest - remainingEvents;

                        logger.verbose("Updating REQUESTED because current work item is terminal. currentRequested[{}]"
                                + " currentWork.remainingEvents[{}] difference[{}]", currentRequest, remainingEvents,
                            difference);

                        return difference < 0 ? 0 : difference;
                    });


                    currentWork = workQueue.poll();
                    continue;
                }

                final SynchronousReceiveWork work = currentWork;
                logger.verbose("workId[{}] numberOfEvents[{}] Current work updated.", work.getId(),
                    work.getNumberOfEvents());

                work.start();
                requestUpstream(work.getNumberOfEvents());

                return work;
            }

            return currentWork;
        }
    }

    /**
     * Adds additional credits upstream if {@code numberOfMessages} is greater than the number of {@code REQUESTED}
     * items.
     *
     * @param numberOfMessages Number of messages required downstream.
     */
    private void requestUpstream(long numberOfMessages) {
        if (isTerminated()) {
            logger.info("Cannot request more messages upstream. Subscriber is terminated.");
            return;
        }

        final Subscription subscription = UPSTREAM.get(this);
        if (subscription == null) {
            logger.info("There is no upstream to request messages from.");
            return;
        }

        final long currentRequested = REQUESTED.get(this);
        final long difference = numberOfMessages - currentRequested;

        logger.verbose("Requesting messages from upstream. currentRequested[{}] numberOfMessages[{}] difference[{}]",
            currentRequested, numberOfMessages, difference);

        if (difference <= 0) {
            return;
        }

        Operators.addCap(REQUESTED, this, difference);

        subscription.request(difference);
    }

    @Override
    public void dispose() {
        super.dispose();

        dispose("Upstream completed the receive work.", null);
    }

    private void dispose(String message, Throwable throwable) {
        super.dispose();

        if (isDisposed.getAndSet(true)) {
            return;
        }

        synchronized (currentWorkLock) {
            if (currentWork != null) {
                currentWork.complete(message, throwable);
                currentWork = null;
            }

            SynchronousReceiveWork w = workQueue.poll();
            while (w != null) {
                w.complete(message, throwable);
                w = workQueue.poll();
            }
        }
    }

    /**
     * package-private method to check queue size.
     *
     * @return The current number of items in the queue.
     */
    int getWorkQueueSize() {
        return this.workQueue.size();
    }
}

