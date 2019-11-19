// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subscriber that takes {@link SynchronousReceiveWork} and publishes events to them in the order received.
 */
public class SynchronousEventSubscriber extends BaseSubscriber<PartitionEvent> {
    private final Timer timer = new Timer("SynchronousEventSubscriber");
    private final AtomicInteger pendingReceives = new AtomicInteger();
    private final ClientLogger logger = new ClientLogger(SynchronousEventSubscriber.class);
    private final SynchronousReceiveWork work;
    private volatile Subscription subscription;

    public SynchronousEventSubscriber(SynchronousReceiveWork work) {
        this.work = Objects.requireNonNull(work, "'work' cannot be null.");
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

        logger.info("Scheduling work item: {}", work.getId());
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

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will pop off
     * that work item, and queue the next one.
     *
     * @param value Event to publish.
     */
    @Override
    protected void hookOnNext(PartitionEvent value) {
        work.next(value);

        if (work.isTerminal()) {
            logger.info("Work: {}, Is completed. Closing Flux and cancelling subscription.", work.getId());
            work.complete();
            subscription.cancel();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnError(Throwable throwable) {
        logger.error("Error occurred in subscriber. Error: {}", throwable);
        work.error(throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        work.complete();
        subscription.cancel();
        super.dispose();
    }

    private class ReceiveTimeoutTask extends TimerTask {
        private final ClientLogger logger = new ClientLogger(ReceiveTimeoutTask.class);
        private final SynchronousReceiveWork work;

        ReceiveTimeoutTask(SynchronousReceiveWork work) {
            this.work = Objects.requireNonNull(work, "'work' cannot be null.");
        }

        @Override
        public void run() {
            logger.info("Timeout task encountered, disposing of subscriber. Work: {}", work.getId());
            SynchronousEventSubscriber.this.dispose();
        }
    }
}

