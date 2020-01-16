// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.Messages;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a synchronous receive request.
 *
 * @see SynchronousEventSubscriber
 */
public class SynchronousReceiveWork {
    private final ClientLogger logger = new ClientLogger(SynchronousReceiveWork.class);
    private final long id;
    private final AtomicInteger remaining;
    private final int numberToReceive;
    private final Duration timeout;
    private final FluxSink<PartitionEvent> emitter;

    private volatile boolean isTerminal = false;

    /**
     * Creates a new synchronous receive work.
     *
     * @param id Identifier for the work.
     * @param numberToReceive Maximum number of events to receive.
     * @param timeout Maximum duration to wait for {@code numberOfReceive} events.
     * @param emitter Sink to publish received events to.
     */
    public SynchronousReceiveWork(long id, int numberToReceive, Duration timeout, FluxSink<PartitionEvent> emitter) {
        this.id = id;
        this.remaining = new AtomicInteger(numberToReceive);
        this.numberToReceive = numberToReceive;
        this.timeout = timeout;
        this.emitter = emitter;
    }

    /**
     * Gets the unique identifier for this work.
     *
     * @return The unique identifier for this work.
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the maximum duration to wait for the work to complete.
     *
     * @return The duration to wait for the work to complete.
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Gets the number of events to receive.
     *
     * @return The number of events to receive.
     */
    int getNumberOfEvents() {
        return numberToReceive;
    }

    /**
     * Gets whether or not the work item has reached a terminal state.
     *
     * @return {@code true} if all the events have been fetched, it has been cancelled, or an error occurred. {@code
     *     false} otherwise.
     */
    boolean isTerminal() {
        return emitter.isCancelled() || remaining.get() == 0 || isTerminal;
    }

    /**
     * Publishes the next event data to a downstream subscriber.
     *
     * @param event Event to publish downstream.
     */
    public void next(PartitionEvent event) {
        try {
            emitter.next(event);
            remaining.decrementAndGet();
        } catch (Exception e) {
            logger.warning(Messages.EXCEPTION_OCCURRED_WHILE_EMITTING, e);
            isTerminal = true;
            emitter.error(e);
        }
    }

    /**
     * Completes the publisher. If the publisher has encountered an error, or an error has occurred, it does nothing.
     */
    public synchronized void complete() {
        if (!isTerminal || emitter.isCancelled()) {
            logger.info("Id: {}. Completing task.", id);
            isTerminal = true;
            emitter.complete();
        }
    }

    /**
     * Publishes an error downstream. This is a terminal step.
     *
     * @param error Error to publish downstream.
     */
    public void error(Throwable error) {
        isTerminal = true;
        emitter.error(error);
    }
}
