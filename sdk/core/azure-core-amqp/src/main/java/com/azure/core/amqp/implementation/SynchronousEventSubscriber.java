// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subscriber that takes {@link SynchronousReceiveWork} and publishes events to them in the order received.
 */
public class SynchronousEventSubscriber extends BaseSubscriber<EventData> {
    private final Timer timer = new Timer("SynchronousEventSubscriber");
    private final AtomicInteger pendingReceives = new AtomicInteger();
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

        if (isEmpty) {
            logger.info("There is no existing work in queue. Scheduling: {}", work.getId());
            scheduleWork(work);
        } else {
            logger.info("Verifying if there are any new work items. {}", work.getId());
            getOrUpdateNextWork();
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

        final SynchronousReceiveWork work = pendingWork.peek();
        if (work == null) {
            logger.warning("There is no work to request EventData for. Listener should have been created with work.");
        } else {
            logger.info("Scheduling first work item: {}", work.getId());
            scheduleWork(work);
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
        SynchronousReceiveWork currentItem = getOrUpdateNextWork();
        if (currentItem == null) {
            logger.warning("EventData received when there is no pending work. Skipping.");
            return;
        }

        pendingReceives.decrementAndGet();
        currentItem.next(value);

        if (currentItem.isTerminal()) {
            logger.info("Work: {}, Is completed. Closing flux.", currentItem.getId());
            currentItem.complete();
            getOrUpdateNextWork();
        }
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

    private synchronized SynchronousReceiveWork getOrUpdateNextWork() {
        SynchronousReceiveWork work = pendingWork.peek();
        if (work == null) {
            subscription.request(0);
        }

        if (work == null || !work.isTerminal()) {
            return work;
        }

        pendingWork.remove(work);
        work = pendingWork.peek();

        if (work == null) {
            subscription.request(0);
            return null;
        }

        scheduleWork(work);
        return work;
    }

    private synchronized void scheduleWork(SynchronousReceiveWork work) {
        if (subscription == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "This has not been subscribed to. Cannot start receiving work."));
        }

        final int pending = work.getNumberOfEvents() - pendingReceives.get();
        logger.info("Work: {}, Pending: {}, Scheduling receive timeout task.", work.getId(), pending);
        if (pending > 0) {
            pendingReceives.addAndGet(pending);
            subscription.request(pending);
        }

        timer.schedule(new ReceiveTimeoutTask(work), work.getTimeout().toMillis());
    }

    private class ReceiveTimeoutTask extends TimerTask {
        private final ClientLogger logger = new ClientLogger(ReceiveTimeoutTask.class);
        private final SynchronousReceiveWork work;

        ReceiveTimeoutTask(SynchronousReceiveWork work) {
            this.work = Objects.requireNonNull(work, "'work' cannot be null.");
        }

        @Override
        public void run() {
            logger.info("Timeout task encountered, disposing of task. Work: {}", work.getId());
            work.complete();
            SynchronousEventSubscriber.this.getOrUpdateNextWork();
        }
    }
}

