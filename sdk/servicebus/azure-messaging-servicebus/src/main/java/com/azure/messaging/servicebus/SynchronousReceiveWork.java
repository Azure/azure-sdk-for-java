// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.WORK_ID_KEY;

/**
 * Synchronous work for receiving messages.
 */
class SynchronousReceiveWork {

    /* When we have received at-least one message and next message does not arrive in this time. The work will
    complete.*/
    private static final Duration TIMEOUT_BETWEEN_MESSAGES = Duration.ofMillis(1000);

    private final ClientLogger logger;
    private final AtomicBoolean isStarted = new AtomicBoolean();
    private final Duration timeout;

    private final long id;
    private final AtomicInteger remaining;
    private final int numberToReceive;

    // Emits the messages downstream.
    private final Sinks.Many<ServiceBusReceivedMessage> downstreamEmitter;

    // Composite subscriptions for both the overall timeout and timeout between messages.
    private final Disposable.Composite timeoutSubscriptions;

    // Indicate state that timeout has occurred for this work.
    private final AtomicBoolean isTerminal = new AtomicBoolean();

    /**
     * Creates a new synchronous receive work.
     *
     * @param id Identifier for the work.
     * @param numberToReceive Maximum number of events to receive.
     * @param timeout Maximum duration to wait for {@code numberOfReceive} events.
     * @param emitter Sink to publish received messages to.
     */
    SynchronousReceiveWork(long id, int numberToReceive, Duration timeout,
        Sinks.Many<ServiceBusReceivedMessage> emitter) {
        this.id = id;
        this.remaining = new AtomicInteger(numberToReceive);
        this.numberToReceive = numberToReceive;
        this.timeout = timeout;
        this.downstreamEmitter = emitter;
        this.timeoutSubscriptions = Disposables.composite();

        Map<String, Object> loggingContext = new HashMap<>(1);
        loggingContext.put(WORK_ID_KEY, id);
        this.logger = new ClientLogger(SynchronousReceiveWork.class, loggingContext);
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
     * Gets the number of events left to receive.
     *
     * @return The number of events to receive.
     */
    int getRemainingEvents() {
        return remaining.get();
    }

    /**
     * Starts the timer for the work.
     */
    synchronized void start() {
        if (isStarted.getAndSet(true)) {
            return;
        }

        this.timeoutSubscriptions.add(
            Mono.delay(timeout).subscribe(
                index -> complete("Timeout elapsed for work."),
                error -> complete("Error occurred while waiting for timeout.", error)));
        this.timeoutSubscriptions.add(
            Flux.switchOnNext(downstreamEmitter.asFlux().map(messageContext -> Mono.delay(TIMEOUT_BETWEEN_MESSAGES)))
                .subscribe(delayElapsed -> {
                    complete("Timeout between the messages occurred. Completing the work.");
                }, error -> {
                    complete("Error occurred while waiting for timeout between messages.", error);
                }));
    }

    /**
     * Gets whether or not the work item has reached a terminal state.
     *
     * @return {@code true} if all the events have been fetched, it has been cancelled, or an error occurred. {@code
     *     false} otherwise.
     */
    synchronized boolean isTerminal() {
        return isTerminal.get();
    }

    /**
     * Publishes the next message to a downstream subscriber.
     *
     * @param message Event to publish downstream.
     *
     * @return true if the work could be emitted downstream. False if it could not be.
     */
    synchronized boolean emitNext(ServiceBusReceivedMessage message) {
        if (isTerminal.get()) {
            return false;
        }

        if (!isStarted.get()) {
            start();
        }

        final int numberLeft = remaining.decrementAndGet();

        if (numberLeft < 0) {
            logger.info("Number left {} < 0. Not emitting downstream.", numberLeft);
            return false;
        }

        final Sinks.EmitResult result = downstreamEmitter.tryEmitNext(message);
        if (result != Sinks.EmitResult.OK) {
            logger.info("Could not emit downstream. EmitResult: {}", result);
            return false;
        }

        // All events are emitted, so complete the synchronous work item. Next loop, it'll return false.
        if (numberLeft == 0) {
            complete(null);
        }

        return true;
    }

    /**
     * Completes the publisher.
     *
     * @param message Message to log.
     */
    void complete(String message) {
        complete(message, null);
    }

    /**
     * Completes the publisher. If the publisher has encountered an error, or an error has occurred, it does nothing.
     *
     * @param message Message to log. Null if there is no message to log.
     * @param error Error if one occurred. Null otherwise.
     */
    void complete(String message, Throwable error) {
        if (isTerminal.getAndSet(true)) {
            return;
        }

        if (message != null) {
            if (error == null) {
                logger.verbose(message);
            } else {
                logger.warning(message, error);
            }
        }

        try {
            timeoutSubscriptions.dispose();
        } finally {
            if (error == null) {
                downstreamEmitter.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            } else {
                downstreamEmitter.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }
    }
}
