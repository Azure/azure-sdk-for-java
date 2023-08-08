// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.Messages;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.util.context.Context;

import java.util.Collections;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.SUBSCRIBER_ID_KEY;

/**
 * Subscriber that takes {@link SynchronousReceiveWork} and publishes events to them in the order received.
 */
public class SynchronousEventSubscriber extends BaseSubscriber<PartitionEvent> {
    private final Timer timer = new Timer();
    private final ClientLogger logger;
    private final SynchronousReceiveWork work;
    private volatile Subscription subscription;
    private final Context context;
    private final String subscriberId;

    public SynchronousEventSubscriber(SynchronousReceiveWork work) {
        this.work = Objects.requireNonNull(work, "'work' cannot be null.");
        this.subscriberId = String.valueOf(work.getId());
        this.context = super.currentContext().put(SUBSCRIBER_ID_KEY, subscriberId);
        this.logger = new ClientLogger(SynchronousEventSubscriber.class, Collections.singletonMap(SUBSCRIBER_ID_KEY, subscriberId));
    }

    @Override
    public Context currentContext() {
        return context;
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

        logger.atInfo()
            .addKeyValue("pendingEvents", work.getNumberOfEvents())
            .log("Scheduling receive timeout task.");
        subscription.request(work.getNumberOfEvents());

        timer.schedule(new ReceiveTimeoutTask(this::dispose, this.logger), work.getTimeout().toMillis());
    }

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will dispose of
     * the subscriber.
     *
     * @param value Event to publish.
     */
    @Override
    protected void hookOnNext(PartitionEvent value) {
        work.next(value);

        if (work.isTerminal()) {
            logger.info("Work completed. Closing Flux and cancelling subscription.");
            dispose();
        }
    }

    @Override
    protected void hookOnComplete() {
        logger.info("Completed. No events to listen to.");
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnError(Throwable throwable) {
        logger.error(Messages.ERROR_OCCURRED_IN_SUBSCRIBER_ERROR, throwable);
        work.error(throwable);
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        work.complete();
        subscription.cancel();
        timer.cancel();
        super.dispose();
    }

    private static class ReceiveTimeoutTask extends TimerTask {
        private final ClientLogger logger;
        private final Runnable onDispose;

        ReceiveTimeoutTask(Runnable onDispose, ClientLogger logger) {
            this.onDispose = onDispose;
            this.logger = logger;
        }

        @Override
        public void run() {
            logger.info("Timeout encountered, disposing of subscriber.");
            onDispose.run();
        }
    }
}

