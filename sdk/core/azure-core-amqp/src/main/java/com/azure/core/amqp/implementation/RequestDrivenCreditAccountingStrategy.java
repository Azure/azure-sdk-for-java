// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The type tracks the downstream request accumulated since the last credit flow to the broker and once the accumulated
 * request is greater than or equal to the Prefetch, send it as the next credit.
 */
final class RequestDrivenCreditAccountingStrategy extends CreditAccountingStrategy {
    private static final int MAX_INT_PREFETCH_BOUND = 100;
    private static final long MAX_LONG_REQUEST_BOUND = 1;
    private long pendingMessageCount;
    private boolean unbounded;
    private final AtomicLong requestAccumulated = new AtomicLong(0);

    /**
     * Create new CreditAccountingStrategy to track the downstream request accumulated and use it to compute
     * the credit to send.
     *
     * @param receiver the receiver for sending credit to the broker.
     * @param subscription the subscription to the receiver's message publisher to request messages when
     *                    needed (the publisher won't translate these requests to network flow performative).
     * @param prefetch the prefetch configured.
     * @param logger the logger.
     */
    RequestDrivenCreditAccountingStrategy(AmqpReceiveLink receiver, Subscription subscription, int prefetch,
        ClientLogger logger) {
        super(receiver, subscription, validateAndGet(prefetch, logger), logger);
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
        if (request == Long.MAX_VALUE) {
            // Once an unbounded downstream request is encountered, future downstream request values, if any, will not
            // be honored per reactive spec.
            // While unbounded requests in the messaging context indicate a wrong downstream usage pattern, we'll
            // adhere to the spec by switching to a safe request constant value MAX_LONG_REQUEST_BOUND for the lifetime
            // of this RequestDrivenCreditAccounting object (ref:MultiSubscriptionSubscriber.produced).
            unbounded = true;
        }
        final long r = unbounded ? MAX_LONG_REQUEST_BOUND : request;
        pendingMessageCount -= emitted;
        final long c = r - pendingMessageCount + prefetch;
        if (c > 0) {
            pendingMessageCount += c;
            subscription.request(c);
            if (requestAccumulated.addAndGet(c) >= prefetch) {
                scheduleCredit(() -> requestAccumulated.getAndSet(0));
            }
        }
    }

    private static int validateAndGet(int prefetch, ClientLogger logger) {
        if (prefetch < 0) {
            throw logger.atInfo().log(new IllegalArgumentException("prefetch >= 0 required but it was " + prefetch));
        }
        return prefetch == Integer.MAX_VALUE ? MAX_INT_PREFETCH_BOUND : prefetch;
    }
}
