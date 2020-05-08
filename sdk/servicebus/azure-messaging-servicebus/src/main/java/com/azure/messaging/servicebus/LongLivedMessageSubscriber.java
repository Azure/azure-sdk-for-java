package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class LongLivedMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessageContext> {

    private final ClientLogger logger = new ClientLogger(SynchronousMessageSubscriber.class);
    private final Timer timer = new Timer();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private Queue<SynchronousReceiveWork> workQueue = new ConcurrentLinkedQueue<>();
    private SynchronousReceiveWork currentWork = null;
    private final AtomicInteger wip = new AtomicInteger();
    private volatile Subscription subscription;


    LongLivedMessageSubscriber(/*SynchronousReceiveWork work*/) {
        //this.work = Objects.requireNonNull(work, "'work' cannot be null.");
        workQueue = new
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

        logger.info("[{}] Pending: {}, Scheduling receive timeout task '{}'.", currentWork.getId(), currentWork.getNumberOfEvents(),
            currentWork.getTimeout());

        //subscription.request(work.getNumberOfEvents());

        //timer.schedule(new LongLivedMessageSubscriber.ReceiveTimeoutTask(work.getId(), this::dispose), work.getTimeout().toMillis());
    }

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will dispose of
     * the subscriber.
     *
     * @param value Event to publish.
     */
    @Override
    protected void hookOnNext(ServiceBusReceivedMessageContext value) {
        currentWork.next(value);

        if (currentWork.isTerminal()) {
            logger.info("[{}] Completed. Closing Flux and cancelling subscription.", currentWork.getId());
            dispose();
        }
    }

    @Override
    protected void hookOnComplete() {
        logger.info("[{}] Completed. No events to listen to.", currentWork.getId());
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnError(Throwable throwable) {
        logger.error("[{}] Errors occurred upstream", currentWork.getId(), throwable);
        currentWork.error(throwable);
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

        currentWork.complete();
        subscription.cancel();
        timer.cancel();
        super.dispose();
    }

    private static final class ReceiveTimeoutTask extends TimerTask {
        private final ClientLogger logger = new ClientLogger(LongLivedMessageSubscriber.ReceiveTimeoutTask.class);
        private final long workId;
        private final Runnable onTimeout;

        ReceiveTimeoutTask(long workId, Runnable onTimeout) {
            this.workId = workId;
            this.onTimeout = onTimeout;
        }

        @Override
        public void run() {
            logger.info("[{}] Timeout encountered, disposing of subscriber.", workId);
            onTimeout.run();
        }
    }

    void queueWork(SynchronousReceiveWork work) {


        this.subscription.request(work.getNumberOfEvents());

        timer.schedule(new LongLivedMessageSubscriber.ReceiveTimeoutTask(work.getId(), this::finishWork), work.getTimeout().toMillis());
    }

    void startWork(SynchronousReceiveWork work) {
        workQueue.add(work);
        drain();
    }

    void finishWork() {

    }

    private void drain() {
        // If someone is already in this loop, then we are already clearing the queue.
        if (!wip.compareAndSet(0, 1)) {
            return;
        }

        try {
            drainQueue();
        } finally {
            if (wip.decrementAndGet() != 0) {
                logger.warning("There is another worker in drainLoop. But there should only be 1 worker.");
            }
        }
    }
}
