// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subscriber that listens to events and publishes them downstream and publishes events to them in the order received.
 */
class SynchronousMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessageContext> {
    private final ClientLogger logger = new ClientLogger(SynchronousMessageSubscriber.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final long prefetch;
    private final AtomicInteger wip = new AtomicInteger();
    private final Queue<SynchronousReceiveWork> workQueue = new ConcurrentLinkedQueue<>();
    private final SynchronousReceiveWork initialWork;
    private volatile Subscription subscription;


    private SynchronousReceiveWork currentWork;
    private Disposable timeoutOperation;
    private Disposable drainQueueDisposable;

    SynchronousMessageSubscriber(long prefetch, SynchronousReceiveWork initWork) {
        this.prefetch = prefetch;
        this.initialWork = initWork;
    }

    /**
     * On an initial subscription, will take the first work item, and request that amount of work for it.
     * @param subscription Subscription for upstream.
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        this.subscription = subscription;

        logger.info("[{}] Pending: {}, Scheduling receive timeout task '{}'.", initialWork.getId(),
            initialWork.getNumberOfEvents(), initialWork.getTimeout());

        // This will trigger subscription.request(N) and queue up the work
        queueWork(initialWork);
    }

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will dispose of
     * the subscriber.
     * @param message Event to publish.
     */
    @Override
    protected void hookOnNext(ServiceBusReceivedMessageContext message) {
        currentWork.next(message);

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
        logger.verbose("[{}] work completed.", currentWork.getId());

        if (timeoutOperation != null && !timeoutOperation.isDisposed()) {
            timeoutOperation.dispose();
        }
        if (drainQueueDisposable != null && !drainQueueDisposable.isDisposed()) {
            drainQueueDisposable.dispose();
        }

        if (wip.decrementAndGet() != 0) {
            logger.warning("There is another worker in drainLoop. But there should only be 1 worker.");
        }

        // After current work finished and there more receive requests
        if (workQueue.size() > 0) {
            drain();
        }
    }

    void queueWork(SynchronousReceiveWork work) {

        logger.info("[{}] Pending: {}, Scheduling receive timeout task '{}'.", work.getId(), work.getNumberOfEvents(),
            work.getTimeout());
        workQueue.add(work);
        drain();
    }

    private void drain() {
        // If someone is already in this loop, then we are already clearing the queue.
        if (!wip.compareAndSet(0, 1)) {
            return;
        }
        // Drain queue..
        drainQueueDisposable = Mono.just(true)
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
        if (isDisposed.getAndSet(true)) {
            return;
        }

        if (currentWork.getError() != null) {
            currentWork.error(currentWork.getError());
        } else {
            currentWork.complete();
        }
        subscription.cancel();
        if (timeoutOperation != null && !timeoutOperation.isDisposed()) {
            timeoutOperation.dispose();
        }
    }

    private boolean isTerminated() {
        return isDisposed.get();
    }
}

