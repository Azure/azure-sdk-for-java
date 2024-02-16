// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;

import java.io.UncheckedIOException;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.ClientConstants.CONNECTION_ID_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;

/**
 * The type that tracks and sends credits for the receiver in message-flux.
 */
abstract class CreditAccountingStrategy {
    private final AmqpReceiveLink receiver;
    protected final ClientLogger logger;
    protected final Subscription subscription;
    protected final int prefetch;

    /**
     * Create new CreditAccountingStrategy to track credit associated with a receiver backing a mediator.
     *
     * @param receiver the receiver for sending credit to the broker.
     * @param subscription the subscription to the receiver's message publisher to request messages when
     *                    needed (the publisher won't translate these requests to network flow performative).
     * @param prefetch the prefetch configured.
     * @param logger the logger.
     */
    protected CreditAccountingStrategy(AmqpReceiveLink receiver, Subscription subscription, int prefetch,
        ClientLogger logger) {
        this.receiver = receiver;
        this.subscription = subscription;
        this.prefetch = prefetch;
        this.logger = logger;
    }

    /**
     * Update the credit accounting based on the latest view of the downstream request and messages emitted by
     * the emitter-loop in the last drain-loop iteration.
     * <br>
     * CONTRACT: Never invoke from the outside of serialized drain-loop in message-flux; the method relies on
     * the thread-safety and memory visibility the drain-loop provides.
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
            receiver.addCredit(creditSupplier);
        } catch (RejectedExecutionException e) {
            log("Scheduling credit flow encountered rejected-error (normal during link termination or transition).", e);
        } catch (UncheckedIOException e) {
            log("Scheduling credit flow encountered io-error (normal during link termination or transition).", e);
        }
    }

    private void log(String message, RuntimeException e) {
        logger.atInfo()
            .addKeyValue(CONNECTION_ID_KEY, receiver.getConnectionId())
            .addKeyValue(LINK_NAME_KEY, receiver.getLinkName())
            .addKeyValue(ENTITY_PATH_KEY, receiver.getEntityPath())
            .log(message, e);
    }
}
