// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Subscriber that listens to events and publishes them downstream and publishes events to them in the order received.
 */
class SynchronousMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessageContext> {
    private final ClientLogger logger = new ClientLogger(SynchronousMessageSubscriber.class);
    private final Timer timer = new Timer();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final SynchronousReceiveWork work;

    private volatile Subscription subscription;

    SynchronousMessageSubscriber(SynchronousReceiveWork work) {
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

        logger.info("[{}] Pending: {}, Scheduling receive timeout task '{}'.", work.getId(), work.getNumberOfEvents(),
            work.getTimeout());

        subscription.request(work.getNumberOfEvents());

        timer.schedule(new ReceiveTimeoutTask(work.getId(), this::dispose), work.getTimeout().toMillis());
    }

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will dispose of
     * the subscriber.
     *
     * @param value Event to publish.
     */
    @Override
    protected void hookOnNext(ServiceBusReceivedMessageContext value) {
        work.next(value);

        if (work.isTerminal()) {
            logger.info("[{}] Completed. Closing Flux and cancelling subscription.", work.getId());
            dispose();
        }
    }

    @Override
    protected void hookOnComplete() {
        logger.info("[{}] Completed. No events to listen to.", work.getId());
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnError(Throwable throwable) {
        logger.error("[{}] Errors occurred upstream", work.getId(), throwable);
        work.error(throwable);
        dispose();
    }

    @Override
    protected void hookOnCancel() {
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        work.complete();
        subscription.cancel();
        timer.cancel();
        super.dispose();
    }

    private static final class ReceiveTimeoutTask extends TimerTask {
        private final ClientLogger logger = new ClientLogger(ReceiveTimeoutTask.class);
        private final long workId;
        private final Runnable onDispose;

        ReceiveTimeoutTask(long workId, Runnable onDispose) {
            this.workId = workId;
            this.onDispose = onDispose;
        }

        @Override
        public void run() {
            logger.info("[{}] Timeout encountered, disposing of subscriber.", workId);
            onDispose.run();
        }
    }
}

