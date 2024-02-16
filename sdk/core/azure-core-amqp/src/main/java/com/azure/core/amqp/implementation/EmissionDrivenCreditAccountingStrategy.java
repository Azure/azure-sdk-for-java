// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The type tracks the number of messages emitted downstream since the last credit flow to the broker, and once
 * the emission count is greater than or equal to a fraction (e.g., 0.5) of the Prefetch, send it as the next credit.
 */
final class EmissionDrivenCreditAccountingStrategy extends CreditAccountingStrategy {
    private static final int MAX_INT_PREFETCH_BOUND = 100;
    private boolean placedInitialCredit;
    private final int limit;
    private final AtomicLong emissionAccumulated = new AtomicLong(0);

    /**
     * Create new CreditAccountingStrategy to track the messages emitted downstream and use it to compute
     * the credit to send.
     *
     * @param receiver the receiver for sending credit to the broker.
     * @param subscription the subscription to the receiver's message publisher to request messages when
     *                    needed (the publisher won't translate these requests to network flow performative).
     * @param prefetch the prefetch configured.
     * @param logger the logger.
     */
    EmissionDrivenCreditAccountingStrategy(AmqpReceiveLink receiver, Subscription subscription, int prefetch,
        ClientLogger logger) {
        super(receiver, subscription, validateAndGet(prefetch, logger), logger);
        // Refill the buffer once 50% of the prefetch has emitted.
        this.limit = this.prefetch - (this.prefetch >> 1);
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
    @Override
    void update(long request, long emitted) {
        if (emitted != 0L) {
            subscription.request(emitted);
            if (emissionAccumulated.addAndGet(emitted) >= limit) {
                scheduleCredit(() -> emissionAccumulated.getAndSet(0));
            }
        } else if (!placedInitialCredit) {
            placedInitialCredit = true;
            subscription.request(prefetch);
            scheduleCredit(() -> Long.valueOf(prefetch));
        }
    }

    private static int validateAndGet(int prefetch, ClientLogger logger) {
        if (prefetch <= 0) {
            throw logger.atInfo().log(new IllegalArgumentException("prefetch >= 1 required but it was " + prefetch));
        }
        return prefetch == Integer.MAX_VALUE ? MAX_INT_PREFETCH_BOUND : prefetch;
    }
}
