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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.SUBSCRIBER_ID_KEY;

/**
 * Subscriber that takes {@link SynchronousReceiveWork} and publishes events to them in the order received.
 */
public class SynchronousEventSubscriber extends BaseSubscriber<PartitionEvent> {
    private static final ScheduledExecutorService TIMEOUT_SCHEDULER = createTimeoutScheduler();

    private final ClientLogger logger;
    private final SynchronousReceiveWork work;
    private volatile Subscription subscription;
    private volatile ScheduledFuture<?> timeoutTask;
    private final Context context;
    private final String subscriberId;

    public SynchronousEventSubscriber(SynchronousReceiveWork work) {
        this.work = Objects.requireNonNull(work, "'work' cannot be null.");
        this.subscriberId = String.valueOf(work.getId());
        this.context = super.currentContext().put(SUBSCRIBER_ID_KEY, subscriberId);
        this.logger = new ClientLogger(SynchronousEventSubscriber.class,
            Collections.singletonMap(SUBSCRIBER_ID_KEY, subscriberId));
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

        logger.atInfo().addKeyValue("pendingEvents", work.getNumberOfEvents()).log("Scheduling receive timeout task.");
        subscription.request(work.getNumberOfEvents());

        timeoutTask = TIMEOUT_SCHEDULER.schedule(new ReceiveTimeoutTask(this::dispose, this.logger),
            work.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
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
        if (subscription != null) {
            subscription.cancel();
        }

        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }

        super.dispose();
    }

    static ScheduledExecutorService getTimeoutScheduler() {
        return TIMEOUT_SCHEDULER;
    }

    private static ScheduledExecutorService createTimeoutScheduler() {
        final ScheduledThreadPoolExecutor scheduler
            = new ScheduledThreadPoolExecutor(1, new ReceiveTimeoutThreadFactory());
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    private static class ReceiveTimeoutTask implements Runnable {
        private final ClientLogger logger;
        private final Runnable onDispose;

        ReceiveTimeoutTask(Runnable onDispose, ClientLogger logger) {
            this.onDispose = onDispose;
            this.logger = logger;
        }

        public void run() {
            logger.info("Timeout encountered, disposing of subscriber.");
            onDispose.run();
        }
    }

    private static class ReceiveTimeoutThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            final Thread thread = new Thread(runnable, "eventhubs-sync-receive-timeout");
            thread.setDaemon(true);
            return thread;
        }
    }
}
