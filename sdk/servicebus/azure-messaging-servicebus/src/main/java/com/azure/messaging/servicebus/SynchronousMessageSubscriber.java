// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Subscriber that listens to events and publishes them downstream and publishes events to them in the order received.
 */
class SynchronousMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessage> {
    private final ClientLogger logger = new ClientLogger(SynchronousMessageSubscriber.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicInteger wip = new AtomicInteger();
    private final Queue<SynchronousReceiveWork> workQueue = new ConcurrentLinkedQueue<>();
    private final Queue<ServiceBusReceivedMessage> bufferMessages = new ConcurrentLinkedQueue<>();
    private final AtomicLong remaining = new AtomicLong();

    private final long requested;
    private final Object currentWorkLock = new Object();

    private Disposable currentTimeoutOperation;
    private SynchronousReceiveWork currentWork;
    private boolean subscriberInitialized;

    private volatile Subscription subscription;

    private static final AtomicReferenceFieldUpdater<SynchronousMessageSubscriber, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(SynchronousMessageSubscriber.class, Subscription.class,
            "subscription");


    SynchronousMessageSubscriber(long prefetch, SynchronousReceiveWork initialWork) {
        this.workQueue.add(initialWork);
        requested = initialWork.getNumberOfEvents() > prefetch ? initialWork.getNumberOfEvents() : prefetch;
    }

    /**
     * On an initial subscription, will take the first work item, and request that amount of work for it.
     * @param subscription Subscription for upstream.
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {

        if (Operators.setOnce(UPSTREAM, this, subscription)) {
            this.subscription = subscription;
            subscriberInitialized = true;
            drain();
        } else {
            logger.error("Already subscribed once.");
        }
    }

    /**
     * Publishes the event to the current {@link SynchronousReceiveWork}. If that work item is complete, will dispose of
     * the subscriber.
     * @param message Event to publish.
     */
    @Override
    protected void hookOnNext(ServiceBusReceivedMessage message) {
        bufferMessages.add(message);
        drain();
    }

    /**
     * Queue the work to be picked up by drain loop.
     * @param work to be queued.
     */
    void queueWork(SynchronousReceiveWork work) {

        logger.info("[{}] Pending: {}, Scheduling receive timeout task '{}'.", work.getId(), work.getNumberOfEvents(),
            work.getTimeout());
        workQueue.add(work);

        // Do not drain if another thread want to queue the work before we have subscriber
        if (subscriberInitialized) {
            drain();
        }
    }

    /**
     * Drain the work, only one thread can be in this loop at a time.
     */
    private void drain() {
        // If someone is already in this loop, then we are already clearing the queue.
        if (!wip.compareAndSet(0, 1)) {
            return;
        }

        try {
            drainQueue();
        } finally {
            final int decremented = wip.decrementAndGet();
            if (decremented != 0) {
                logger.warning("There should be 0, but was: {}", decremented);
            }
        }
    }

    /***
     * Drain the queue using a lock on current work in progress.
     */
    private void drainQueue() {
        if (isTerminated()) {
            return;
        }

        // Acquiring the lock
        synchronized (currentWorkLock) {

            // Making sure current work not become terminal since last drain queue cycle
            if (currentWork != null && currentWork.isTerminal()) {
                workQueue.remove(currentWork);
                if (currentTimeoutOperation != null && !currentTimeoutOperation.isDisposed()) {
                    currentTimeoutOperation.dispose();
                }
                currentTimeoutOperation = null;
            }

            // We should process a work when
            // 1. it is first time getting picked up
            // 2. or more messages have arrived while we were in drain loop.
            // We might not have all the message in bufferMessages needed for workQueue, Thus we will only remove work
            // from queue when we have delivered all the messages to currentWork.

            while ((currentWork = workQueue.peek()) != null
                && (!currentWork.isProcessingStarted() || bufferMessages.size() > 0)) {
                // Additional check for safety, but normally this work should never be terminal
                if (currentWork.isTerminal()) {
                    // This work already finished by either timeout or no more messages to send, process next work.
                    workQueue.remove(currentWork);
                    if (currentTimeoutOperation != null && !currentTimeoutOperation.isDisposed()) {
                        currentTimeoutOperation.dispose();
                    }
                    continue;
                }

                if (!currentWork.isProcessingStarted()) {
                    // timer to complete the currentWork in case of timeout trigger
                    currentTimeoutOperation = getTimeoutOperation(currentWork);
                    currentWork.startedProcessing();
                    final long calculatedRequest = currentWork.getNumberOfEvents() - remaining.get();
                    remaining.addAndGet(calculatedRequest);
                    subscription.request(calculatedRequest);
                }

                // Send messages to currentWork from buffer
                while (bufferMessages.size() > 0 && !currentWork.isTerminal()) {
                    currentWork.next(bufferMessages.poll());
                    remaining.decrementAndGet();
                }

                // if  we have delivered all the messages to currentWork, we will complete it.
                if (currentWork.isTerminal()) {
                    if (currentWork.getError() == null) {
                        currentWork.complete();
                    }
                    // Now remove from queue since it is complete
                    workQueue.remove(currentWork);
                    if (currentTimeoutOperation != null && !currentTimeoutOperation.isDisposed()) {
                        currentTimeoutOperation.dispose();
                    }
                    logger.verbose("The work [{}] is complete.", currentWork.getId());
                }
            }
        }
    }

    /**
     * @param work on which timeout thread need to start.
     *
     * @return {@link Disposable} for the timeout operation.
     */
    private Disposable getTimeoutOperation(SynchronousReceiveWork work) {
        Duration timeout = work.getTimeout();
        return Mono.delay(timeout).thenReturn(work)
            .subscribe(l -> {
                synchronized (currentWorkLock) {
                    if (currentWork == work) {
                        work.timeout();
                    }
                }
            });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookOnError(Throwable throwable) {
        logger.error("[{}] Errors occurred upstream", currentWork.getId(), throwable);
        synchronized (currentWorkLock) {
            currentWork.error(throwable);
        }
        dispose();
    }

    @Override
    protected void hookOnCancel() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        synchronized (currentWorkLock) {
            if (currentWork != null) {
                currentWork.complete();
            }
            if (currentTimeoutOperation != null && !currentTimeoutOperation.isDisposed()) {
                currentTimeoutOperation.dispose();
            }
            currentTimeoutOperation = null;
        }

        subscription.cancel();
    }

    private boolean isTerminated() {
        return isDisposed.get();
    }

    int getWorkQueueSize() {
        return this.workQueue.size();
    }

    long getRequested() {
        return this.requested;
    }

    boolean isSubscriberInitialized() {
        return this.subscriberInitialized;
    }
}
