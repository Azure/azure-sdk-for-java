// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Synchronous work for receiving messages.
 */
class SynchronousReceiveWork {

    /* When we have received at-least one message and next message does not arrive in this time. The work will
    complete.*/
    private static final Duration TIMEOUT_BETWEEN_MESSAGES = Duration.ofMillis(1000);

    private final ClientLogger logger = new ClientLogger(SynchronousReceiveWork.class);
    private final long id;
    private final AtomicInteger remaining;
    private final int numberToReceive;
    private final Duration timeout;

    // Emits the messages downstream.
    private final Sinks.Many<ServiceBusReceivedMessage> downstreamEmitter;

    // Subscribes to next message from upstream and implement short timeout between the messages.
    private final Disposable nextMessageTimeoutSubscription;

    // Indicate state that timeout has occurred for this work.
    private final AtomicBoolean isCompleted = new AtomicBoolean();

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
        this.nextMessageTimeoutSubscription =
            Flux.switchOnNext(emitter.asFlux().map(messageContext -> Mono.delay(TIMEOUT_BETWEEN_MESSAGES)))
                .subscribe(delayElapsed -> {
                    logger.info("[{}]: Timeout between the messages occurred. Completing the work.", id);
                    emitter.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                });
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
     * Gets whether or not the work item has reached a terminal state.
     *
     * @return {@code true} if all the events have been fetched, it has been cancelled, or an error occurred. {@code
     *     false} otherwise.
     */
    boolean isTerminal() {
        return isCompleted.get();
    }

    /**
     * Publishes the next message to a downstream subscriber.
     *
     * @param message Event to publish downstream.
     *
     * @return true if the work could be emitted downstream. False if it could not be.
     */
    boolean emitNext(ServiceBusReceivedMessage message) {
        if (isCompleted.get()) {
            return false;
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
            close(null);
        }

        return true;
    }

    /**
     * Completes the publisher. If the publisher has encountered an error, or an error has occurred, it does nothing.
     */
    void complete() {
        if (isCompleted.get()) {
            return;
        }

        logger.info("[{}]: Upstream completed the receive work.", id);
        close(null);
    }

    /**
     * Completes the publisher and sets the state to timeout.
     */
    void timeout() {
        if (isCompleted.get()) {
            return;
        }

        logger.info("[{}]: Upstream operation timeout occurred. Completing the work.", id);
        close(null);
    }

    /**
     * Publishes an error downstream. This is a terminal step.
     *
     * @param error Error to publish downstream.
     */
    void error(Throwable error) {
        close(error);
    }

    void close(Throwable error) {
        if (isCompleted.getAndSet(true)) {
            return;
        }
        try {
            nextMessageTimeoutSubscription.dispose();

        } finally {
            if (error == null) {
                downstreamEmitter.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            } else {
                downstreamEmitter.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }
    }
}
