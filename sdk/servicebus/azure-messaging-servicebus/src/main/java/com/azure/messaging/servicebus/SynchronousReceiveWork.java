// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Synchronous work for receiving messages.
 */
class SynchronousReceiveWork implements AutoCloseable {

    /* When we have received at-least one message and next message does not arrive in this time. The work will
    complete.*/
    private static final Duration TIMEOUT_BETWEEN_MESSAGES = Duration.ofMillis(1000);

    private final ClientLogger logger = new ClientLogger(SynchronousReceiveWork.class);
    private final long id;
    private final AtomicInteger remaining;
    private final int numberToReceive;
    private final Duration timeout;
    private final FluxSink<ServiceBusReceivedMessageContext> emitter;
    private final FluxSink<ServiceBusReceivedMessageContext> messageReceivedSink;
    private final DirectProcessor<ServiceBusReceivedMessageContext> emitterProcessor;
    // Subscribes to next message from upstream and implement short timeout between the messages.
    private final Disposable nextMessageSubscriber;

    // Indicate state that timeout has occurred for this work.
    private boolean workTimedOut = false;

    // Indicate that if processing started or not.
    private boolean processingStarted;

    private volatile Throwable error = null;

    /**
     * Creates a new synchronous receive work.
     *
     * @param id Identifier for the work.
     * @param numberToReceive Maximum number of events to receive.
     * @param timeout Maximum duration to wait for {@code numberOfReceive} events.
     * @param emitter Sink to publish received messages to.
     */
    SynchronousReceiveWork(long id, int numberToReceive, Duration timeout,
        FluxSink<ServiceBusReceivedMessageContext> emitter) {
        this.id = id;
        this.remaining = new AtomicInteger(numberToReceive);
        this.numberToReceive = numberToReceive;
        this.timeout = timeout;
        this.emitter = emitter;

        emitterProcessor = DirectProcessor.create();
        messageReceivedSink = emitterProcessor.sink();

        nextMessageSubscriber = Flux.switchOnNext(emitterProcessor.map(messageContext ->
            Flux.interval(TIMEOUT_BETWEEN_MESSAGES)))
            .handle((delay, sink) -> {
                logger.info("[{}]: Timeout between the messages occurred. Completing the work.", id);
                sink.next(delay);
                emitter.complete();
            })
        .subscribe();
    }

    /**
     * Gets the unique identifier for this work.
     *
     * @return The unique identifier for this work.
     */
    long getId() {
        return id;
    }

    /**
     * Gets the maximum duration to wait for the work to complete.
     *
     * @return The duration to wait for the work to complete.
     */
    Duration getTimeout() {
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
     * @return remaining events to receive.
     */
    int getRemaining() {
        return remaining.get();
    }

    /**
     * Gets whether or not the work item has reached a terminal state.
     *
     * @return {@code true} if all the events have been fetched, it has been cancelled, or an error occurred. {@code
     *     false} otherwise.
     */
    boolean isTerminal() {
        return emitter.isCancelled() || remaining.get() == 0 || error != null || workTimedOut;
    }

    /**
     * Publishes the next message to a downstream subscriber.
     *
     * @param message Event to publish downstream.
     */
    void next(ServiceBusReceivedMessageContext message) {
        try {
            emitter.next(message);
            messageReceivedSink.next(message);
            remaining.decrementAndGet();
        } catch (Exception e) {
            logger.warning("Exception occurred while publishing downstream.", e);
            error(e);
        }
    }

    /**
     * Completes the publisher. If the publisher has encountered an error, or an error has occurred, it does nothing.
     */
    void complete() {
        logger.info("[{}]: Completing task.", id);
        emitter.complete();
        close();
    }

    /**
     * Completes the publisher and sets the state to timeout.
     */
    void timeout() {
        logger.info("[{}]: Work timeout occurred. Completing the work.", id);
        emitter.complete();
        workTimedOut = true;
        close();
    }

    /**
     * Publishes an error downstream. This is a terminal step.
     *
     * @param error Error to publish downstream.
     */
    void error(Throwable error) {
        this.error = error;
        emitter.error(error);
        close();
    }

    /**
     * Returns the error object.
     * @return the error.
     */
    Throwable getError() {
        return this.error;
    }

    /**
     * Indiate that processing is started for this work.
     */
    void startedProcessing() {
        this.processingStarted = true;
    }

    /**
     *
     * @return flag indicting that processing is started or not.
     */
    boolean isProcessingStarted() {
        return this.processingStarted;
    }

    @Override
    public void close() {
        if (!nextMessageSubscriber.isDisposed()) {
            nextMessageSubscriber.dispose();
        }
    }
}
