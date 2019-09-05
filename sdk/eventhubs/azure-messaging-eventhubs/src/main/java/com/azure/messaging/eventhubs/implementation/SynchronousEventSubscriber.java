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

public class SynchronousEventSubscriber extends BaseSubscriber<EventData> {
    private final Timer timer = new Timer("SynchronousEventSubscriber");
    private final ClientLogger logger = new ClientLogger(SynchronousEventSubscriber.class);
    private final Queue<SynchronousReceiveWork> pendingWork = new ConcurrentLinkedQueue<>();
    private volatile Subscription subscription;

    public SynchronousEventSubscriber(SynchronousReceiveWork receiveItem) {
        Objects.requireNonNull(receiveItem, "'receiveItem' cannot be null.");
        pendingWork.add(receiveItem);
    }

    /**
     * Adds a new receive work item to the queue.
     *
     * @param work Synchronous receive work to add to the queue.
     */
    public synchronized void queueReceiveWork(SynchronousReceiveWork work) {
        Objects.requireNonNull(work, "'work' cannot be null.");

        final boolean isEmpty = pendingWork.isEmpty();
        pendingWork.add(work);

        // There was no work before we added this new work item.
        if (isEmpty) {
            updateWork(work);
        }
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        if (this.subscription == null) {
            this.subscription = subscription;
        }

        final SynchronousReceiveWork currentWork = pendingWork.peek();
        if (currentWork == null) {
            logger.warning("There is no work to request EventData for. Listener should have been created with work.");
        } else {
            subscription.request(currentWork.getNumberOfEvents());
        }
    }

    @Override
    protected void hookOnNext(EventData value) {
        final SynchronousReceiveWork currentItem = pendingWork.peek();
        if (currentItem == null) {
            logger.warning("EventData received when there is no pending work. Skipping.");
            return;
        }

        currentItem.next(value);

        if (!currentItem.isComplete()) {
            return;
        }

        currentItem.complete();
        pendingWork.remove();

        final SynchronousReceiveWork nextWork = pendingWork.peek();
        updateWork(nextWork);
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        logger.error("Error occurred in subscriber.", throwable);
        final SynchronousReceiveWork[] remainingWork = pendingWork.toArray(new SynchronousReceiveWork[0]);
        pendingWork.clear();

        for (SynchronousReceiveWork work : remainingWork) {
            work.error(throwable);
        }
    }

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

