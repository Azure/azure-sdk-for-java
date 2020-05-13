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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Subscriber that listens to events and publishes them downstream and publishes events to them in the order received.
 */
class SynchronousMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessageContext> {
    private final ClientLogger logger = new ClientLogger(SynchronousMessageSubscriber.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final long prefetch;
    private final AtomicInteger wip = new AtomicInteger();
    private final Queue<SynchronousReceiveWork> workQueue = new ConcurrentLinkedQueue<>();
    private final Queue<ServiceBusReceivedMessageContext> bufferMessages = new ConcurrentLinkedQueue<>();
    private final SynchronousReceiveWork initialWork;
    private final AtomicLong remaining = new AtomicLong();

    private volatile Subscription subscription;

    private SynchronousReceiveWork currentWork;
    private Disposable timeoutOperation;

    SynchronousMessageSubscriber(long prefetch, SynchronousReceiveWork initialWork) {
        this.prefetch = prefetch;
        this.initialWork = initialWork;
    }

    /**
     * On an initial subscription, will take the first work item, and request that amount of work for it.
     * @param subscription Subscription for upstream.
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        this.subscription = subscription;

        logger.info("[{}] onSubscribe Pending: {}, Scheduling receive timeout task '{}'.", initialWork.getId(),
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
        if (currentWork == null) {
            //Boundary condition(timeout cases), buffer the received message for future requests.
            bufferMessages.add(message);
            return;
        }
        currentWork.next(message);
        remaining.decrementAndGet();

        if (currentWork.isTerminal()) {
            currentWork.complete();
            if (timeoutOperation != null && !timeoutOperation.isDisposed()) {
                timeoutOperation.dispose();
            }

            // Now see if there is more queued up work
            currentWork = workQueue.poll();
            if (currentWork != null) {

                logger.verbose("[{}] Picking up next receive request.", currentWork.getId());

                // timer to complete the current in case of timeout trigger
                timeoutOperation = getTimeoutOperation();

                requestCredits(currentWork.getNumberOfEvents());
            } else {
                if (wip.decrementAndGet() != 0) {
                    logger.warning("There is another worker in drainLoop. But there should only be 1 worker.");
                }
            }
        }
    }

    /**
     *
     * @param requested credits for current {@link SynchronousReceiveWork}.
     */
    private void requestCredits(long requested) {
        long creditToAdd = requested - (remaining.get() + bufferMessages.size());
        if (creditToAdd > 0) {
            remaining.addAndGet(creditToAdd);
            subscription.request(creditToAdd);
        } else {
            logger.verbose("[{}] No need to request credit. ", currentWork.getId());
        }

    }

    void queueWork(SynchronousReceiveWork work) {

        logger.info("[{}] Pending: {}, Scheduling receive timeout task '{}'.", work.getId(), work.getNumberOfEvents(),
            work.getTimeout());
        workQueue.add(work);
        drain();
    }

    private void drain() {
        if (workQueue.size() == 0) {
            return;
        }

        // If someone is already in this loop, then we are already clearing the queue.
        if (!wip.compareAndSet(0, 1)) {
            return;
        }

        // Drain queue..
        drainQueue();

    }


    private void drainQueue() {
        if (isTerminated()) {
            return;
        }
        currentWork = workQueue.poll();
        if (currentWork == null) {
            return;
        }
        long sentFromBuffer = 0;
        if (bufferMessages.size() > 0) {
            // If  we already have messages in buffer, we should send it first

            while (!bufferMessages.isEmpty() || sentFromBuffer < currentWork.getNumberOfEvents()) {
                currentWork.next(bufferMessages.poll());
                remaining.decrementAndGet();
                ++sentFromBuffer;
            }
            if (sentFromBuffer == currentWork.getNumberOfEvents()) {
                currentWork.complete();
                logger.verbose("[{}] Sent [{}] messages from buffer.", currentWork.getId(), sentFromBuffer);
                drainQueue();
            }
        }
        // timer to complete the current in case of timeout trigger
        timeoutOperation = getTimeoutOperation();

        requestCredits(currentWork.getNumberOfEvents() - sentFromBuffer);
    }

    private Disposable getTimeoutOperation() {
        return Mono.delay(currentWork.getTimeout())
            .subscribe(l -> {
                if (currentWork != null && !currentWork.isTerminal()) {
                    logger.verbose("[{}] Timeout triggered.", currentWork.getId());
                    currentWork.complete();
                }
                if (wip.decrementAndGet() != 0) {
                    logger.warning("There is another worker in drainLoop. But there should only be 1 worker.");
                }
                //any work which needs to be processed.
                drain();
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

        if (currentWork != null) {
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
