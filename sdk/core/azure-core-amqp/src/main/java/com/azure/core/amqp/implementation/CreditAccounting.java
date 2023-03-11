// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;

import java.io.UncheckedIOException;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

/**
 * The type that tracks and sends credits for the receiver in message-flux.
 */
abstract class CreditAccounting {
    private final AmqpReceiveLink receiver;
    protected final ClientLogger logger;
    protected final Subscription subscription;
    protected final int prefetch;

    /**
     * Create new CreditAccounting to track credit associated with a receiver backing a mediator.
     *
     * @param receiver the receiver for sending credit to the broker.
     * @param subscription the subscription to the receiver's message publisher to request messages when
     *                    needed (the publisher won't translate these requests to network flow performative).
     * @param prefetch the prefetch configured.
     * @param logger the logger.
     */
    protected CreditAccounting(AmqpReceiveLink receiver, Subscription subscription, int prefetch, ClientLogger logger) {
        this.receiver = receiver;
        this.subscription = subscription;
        this.prefetch = prefetch;
        this.logger = logger;
    }

    /**
     * CONTRACT: Never invoke from the outside of serialized drain-loop of message-flux.
     * <br/>
     * Notify the latest view of the downstream request and messages emitted by the emitter-loop during
     * the last drain-loop iteration in message-flux.
     *
     * @param request the latest view of the downstream request.
     * @param emitted the number of messages emitted by the latest emitter-loop run.
     */
    abstract void update(long request, long emitted);

    /**
     * Request receiver to schedule an event to send a credit to the broker.
     *
     * @param creditSupplier the supplier that supplies the credit to send.
     */
    protected void scheduleCredit(Supplier<Long> creditSupplier) {
        try {
            receiver.scheduleCredit(creditSupplier);
        } catch (RejectedExecutionException e) {
            logger.info("RejectedExecutionException when attempting to schedule credit flow.", e);
        } catch (UncheckedIOException e) {
            logger.info("UncheckedIOException when attempting to schedule credit flow.", e);
        }
    }
}
