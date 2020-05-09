package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class LongLivedMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessageContext> {

    private final ClientLogger logger = new ClientLogger(SynchronousMessageSubscriber.class);
    private Disposable timeoutOperation;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private Queue<SynchronousReceiveWork> workQueue = new ConcurrentLinkedQueue<>();
    private SynchronousReceiveWork currentWork = null;
    private final AtomicInteger wip = new AtomicInteger();
    private volatile Subscription subscription;
    private long prefetch;


    LongLivedMessageSubscriber(long prefetch) {
        this.prefetch = prefetch;
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
    }

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will dispose of
     * the subscriber.
     *
     * @param value Event to publish.
     */
    @Override
    protected void hookOnNext(ServiceBusReceivedMessageContext value) {
        if (!currentWork.isTerminal()) {
            currentWork.next(value);
        } else {
            logger.error("[{}] received message but no subscriber Sequence number [{}].", currentWork.getId(), value.getMessage().getSequenceNumber());
            // throw error since we can not send this to current receive.
        }

        logger.info("[{}] received message with Sequence Number [{}].", currentWork.getId(), value.getMessage().getSequenceNumber());

        if (currentWork.isTerminal()) {
            logger.info("[{}] Completed. Closing Flux and cancelling subscription.", currentWork.getId());
            completeCurrentWork(currentWork);
        }
    }


    private void completeCurrentWork(SynchronousReceiveWork currentWork) {

        if (isTerminated()) {
            return;
        }

        currentWork.complete();

        if (timeoutOperation != null && !timeoutOperation.isDisposed()) {
            timeoutOperation.dispose();
        }
        if (wip.decrementAndGet() != 0) {
            logger.warning("There is another worker in drainLoop. But there should only be 1 worker. Value:"+wip.get());
        }

        // After current work finished and there more receive requests
        if (workQueue.size() > 0 ) {
            drain();
        }
    }

    void queueWork(SynchronousReceiveWork work) {
        workQueue.add(work);
        drain();
    }

    private void drain() {
        // If someone is already in this loop, then we are already clearing the queue.
        if (!wip.compareAndSet(0, 1)) {
            return;
        }
        // Drain queue..
        Disposable drainQueueDisposable = Mono.just(true)
            .subscribe(l -> {
                drainQueue();
            });
    }

    private void drainQueue() {
        if (isTerminated()) {
            return;
        }
        currentWork = workQueue.poll();
        if (currentWork == null) {
            return;
        }
        subscription.request(currentWork.getNumberOfEvents());

        // timer to complete the current in case of timeout trigger
        timeoutOperation = Mono.delay(currentWork.getTimeout())
            .subscribe(l -> {
                if (!currentWork.isTerminal()) {
                    completeCurrentWork(currentWork);
                }
            });
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
        if (timeoutOperation != null && !timeoutOperation.isDisposed() ) {
            timeoutOperation.dispose();
        }
        super.dispose();
    }

    private boolean isTerminated(){
        return isDisposed.get();
    }
}
