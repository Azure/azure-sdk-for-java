// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.LOCK_TOKEN_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.NUMBER_OF_REQUESTED_MESSAGES_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.WORK_ID_KEY;

/**
 * Subscriber that listens to events and publishes them downstream and publishes events to them in the order received.
 */
class SynchronousMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessage> {
    private static final ClientLogger LOGGER = new ClientLogger(SynchronousMessageSubscriber.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicInteger wip = new AtomicInteger();
    private final ConcurrentLinkedQueue<SynchronousReceiveWork> workQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedDeque<ServiceBusReceivedMessage> bufferMessages = new ConcurrentLinkedDeque<>();

    private final Object currentWorkLock = new Object();
    private final ServiceBusReceiverAsyncClient asyncClient;
    private final boolean isPrefetchDisabled;
    private final Duration operationTimeout;

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
     *
     * @param asyncClient Client to update disposition of messages.
     * @param isPrefetchDisabled Indicates if the prefetch is disabled.
     * @param operationTimeout Timeout to wait for operation to complete.
     * @param initialWork Initial work to queue.
     *
     * <p>
     * When {@code isPrefetchDisabled} is true, we release the messages those received during the timespan
     * between the last terminated downstream and the next active downstream.
     * </p>
     *
     * @throws NullPointerException if {@code initialWork} is null.
     * @throws IllegalArgumentException if {@code initialWork.getNumberOfEvents()} is less than 1.
     */
    SynchronousMessageSubscriber(ServiceBusReceiverAsyncClient asyncClient,
                                 SynchronousReceiveWork initialWork,
                                 boolean isPrefetchDisabled,
                                 Duration operationTimeout) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");
        this.workQueue.add(Objects.requireNonNull(initialWork, "'initialWork' cannot be null."));

        this.isPrefetchDisabled = isPrefetchDisabled;
        if (initialWork.getNumberOfEvents() < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
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
            LOGGER.warning("This should only be subscribed to once. Ignoring subscription.");
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
        if (isTerminated()) {
            Operators.onNextDropped(message, Context.empty());
        } else {
            bufferMessages.add(message);
            drain();
        }
    }

    /**
     * Queue the work to be picked up by drain loop.
     *
     * @param work to be queued.
     */
    void queueWork(SynchronousReceiveWork work) {
        Objects.requireNonNull(work, "'work' cannot be null");

        workQueue.add(work);

        LoggingEventBuilder logBuilder = LOGGER.atVerbose()
            .addKeyValue(WORK_ID_KEY, work.getId())
            .addKeyValue("numberOfEvents", work.getNumberOfEvents())
            .addKeyValue("timeout", work.getTimeout());

        // If previous work items were completed, the message queue is empty, and currentWork is null or terminal,
        // Update the current work and request items upstream if we need to.
        if (workQueue.peek() == work && (currentWork == null || currentWork.isTerminal())) {
            logBuilder.log("First work in queue. Requesting upstream if needed.");
            getOrUpdateCurrentWork();
        } else {
            logBuilder.log("Queuing receive work.");
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

        SynchronousReceiveWork currentDownstream = null;
        while (numberRequested != 0L && !isEmpty) {
            if (isTerminated()) {
                break;
            }

            // Track the number of messages read from the buffer those are either emitted to downstream or released.
            long numberConsumed = 0L;
            // Iterate and attempt to read the requested number of events.
            while (numberRequested != numberConsumed) {
                if (isEmpty || isTerminated()) {
                    break;
                }

                final ServiceBusReceivedMessage message = bufferMessages.poll();

                // While there are messages in the buffer, obtain the current (unterminated) downstream and
                // attempt to emit the message to it.
                boolean isEmitted = false;
                while (!isEmitted) {
                    currentDownstream = getOrUpdateCurrentWork();
                    if (currentDownstream == null) {
                        break;
                    }

                    isEmitted = currentDownstream.emitNext(message);
                }

                if (!isEmitted) {
                    // The only reason we can't emit was the downstream(s) were terminated hence nobody
                    // to receive the message.
                    if (isPrefetchDisabled) {
                        // release is enabled only for no-prefetch scenario.
                        asyncClient.release(message).subscribe(__ -> { },
                            error -> LOGGER.atWarning()
                                .addKeyValue(LOCK_TOKEN_KEY, message.getLockToken())
                                .log("Couldn't release the message.", error),
                            () -> LOGGER.atVerbose()
                                .addKeyValue(LOCK_TOKEN_KEY, message.getLockToken())
                                .log("Message successfully released."));
                    } else {
                        // Re-buffer the message as it couldn't be emitted or release was disabled.
                        bufferMessages.addFirst(message);
                        break;
                    }
                }

                // account for consumed message - message is either emitted or released.
                numberConsumed++;
                isEmpty = bufferMessages.isEmpty();
            }

            final long requestedMessages = REQUESTED.get(this);
            if (requestedMessages != Long.MAX_VALUE) {
                numberRequested = REQUESTED.addAndGet(this, -numberConsumed);
            }
        }
        if (numberRequested == 0L) {
            LOGGER.atVerbose()
                .log("Current work is completed. Schedule next work.");
            getOrUpdateCurrentWork();
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
            //The work in queue will not be terminal, here is double check
            while (currentWork != null && currentWork.isTerminal()) {
                LOGGER.atVerbose()
                    .addKeyValue(WORK_ID_KEY, currentWork.getId())
                    .addKeyValue("numberOfEvents", currentWork.getNumberOfEvents())
                    .log("This work from queue is terminal. Skip it.");

                currentWork = workQueue.poll();
            }

            if (currentWork != null) {
                final SynchronousReceiveWork work = currentWork;
                LOGGER.atVerbose()
                    .addKeyValue(WORK_ID_KEY, work.getId())
                    .addKeyValue("numberOfEvents", work.getNumberOfEvents())
                    .log("Current work updated.");

                work.start();

                // Now that we updated REQUESTED to account for credits already on the line, we're good to
                // place any credits for this new work item.
                requestUpstream(work.getNumberOfEvents());
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
            LOGGER.info("Cannot request more messages upstream. Subscriber is terminated.");
            return;
        }

        final Subscription subscription = UPSTREAM.get(this);
        if (subscription == null) {
            LOGGER.info("There is no upstream to request messages from.");
            return;
        }

        final long currentRequested = REQUESTED.get(this);
        final long difference = numberOfMessages - currentRequested;

        LOGGER.atVerbose()
            .addKeyValue(NUMBER_OF_REQUESTED_MESSAGES_KEY, currentRequested)
            .addKeyValue("numberOfMessages", numberOfMessages)
            .addKeyValue("difference", difference)
            .log("Requesting messages from upstream.");

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

