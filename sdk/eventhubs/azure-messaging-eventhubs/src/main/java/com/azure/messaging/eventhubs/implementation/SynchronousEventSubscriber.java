// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Subscriber that takes {@link SynchronousReceiveWork} and publishes events to them in the order received.
 */
public class SynchronousEventSubscriber extends BaseSubscriber<EventData> {
    private final Timer timer = new Timer("SynchronousEventSubscriber");
    private final ClientLogger logger = new ClientLogger(SynchronousEventSubscriber.class);
    private final Queue<SynchronousReceiveWork> pendingWork = new ConcurrentLinkedQueue<>();
    private volatile Subscription subscription;

    /**
     * Creates an instance with an initial receive work item.
     *
     * @param work Initial work item to start publishing to.
     */
    public SynchronousEventSubscriber(SynchronousReceiveWork work) {
        Objects.requireNonNull(work, "'receiveItem' cannot be null.");
        pendingWork.add(work);
    }

    /**
     * Adds a new receive work item to the queue.
     *
     * @param work Synchronous receive work to add to the queue.
     */
    public void queueReceiveWork(SynchronousReceiveWork work) {
        Objects.requireNonNull(work, "'work' cannot be null.");

        final boolean isEmpty = pendingWork.isEmpty();
        pendingWork.add(work);

        // There was no work before we added this new work item.
        if (isEmpty) {
            updateWork(work);
        }
    }

    /**
     * On an initial subscription, will take the first work item, and request that amount of work for it.
     *
     * @param subscription Subscription for upstream.
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        if (this.subscription == null) {
            this.subscription = subscription;
        }

        final SynchronousReceiveWork currentWork = pendingWork.peek();
        if (currentWork == null) {
            logger.warning("There is no work to request EventData for. Listener should have been created with work.");
        } else {
            logger.info("Starting subscription with work: {}", currentWork.getId());
            subscription.request(currentWork.getNumberOfEvents());
        }
    }

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will pop off
     * that work item, and queue the next one.
     *
     * @param value Event to publish.
     */
    @Override
    protected void hookOnNext(EventData value) {
        SynchronousReceiveWork currentItem = pendingWork.peek();
        if (currentItem == null) {
            logger.warning("EventData received when there is no pending work. Skipping.");
            return;
        }

        if (!currentItem.isComplete()) {
            currentItem.next(value);
        } else {
            pendingWork.remove(currentItem);
            currentItem = pendingWork.peek();

            if (currentItem == null) {
                logger.warning("Current work completed before this value was seen. There is no more pending work.");
                return;
            }
        }

        if (!currentItem.isComplete()) {
            return;
        }

        currentItem.complete();
        pendingWork.remove();

        final SynchronousReceiveWork nextWork = pendingWork.peek();
        updateWork(nextWork);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnError(Throwable throwable) {
        logger.error("Error occurred in subscriber.", throwable);
        final SynchronousReceiveWork[] remainingWork = pendingWork.toArray(new SynchronousReceiveWork[0]);
        pendingWork.clear();

        for (SynchronousReceiveWork work : remainingWork) {
            work.error(throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        final SynchronousReceiveWork[] remainingWork = pendingWork.toArray(new SynchronousReceiveWork[0]);
        pendingWork.clear();

        for (SynchronousReceiveWork work : remainingWork) {
            work.complete();
        }

        super.dispose();
    }

    private synchronized void updateWork(SynchronousReceiveWork work) {
        if (work == null) {
            return;
        }

        if (subscription == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "This has not been subscribed to. Cannot start receiving work."));
        }

        logger.info("Scheduling receiver for: {}", work.getId());
        subscription.request(work.getNumberOfEvents());
        timer.schedule(new ReceiveTimeoutTask(work), work.getTimeout().toMillis());
    }

    private static class ReceiveTimeoutTask extends TimerTask {
        private final SynchronousReceiveWork work;

        ReceiveTimeoutTask(SynchronousReceiveWork work) {
            this.work = Objects.requireNonNull(work, "'work' cannot be null.");
        }

        @Override
        public void run() {
            if (!work.isComplete()) {
                work.complete();
            }
        }
    }
}

