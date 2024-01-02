// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

/**
 * Indicate how {@link ReceiverDeliveryHandler} should locally settle the deliveries received from the broker.
 */
public enum DeliverySettleMode {
    /**
     * Locally settle the delivery as soon as it is received.
     */
    SETTLE_ON_DELIVERY,
    /**
     * Mark as Accepted and locally settle the delivery as soon as it is received.
     */
    ACCEPT_AND_SETTLE_ON_DELIVERY,
    /**
     * The application should first request the delivery settlement on the broker, then locally
     * settle depending on the broker's ack.
     */
    SETTLE_VIA_DISPOSITION
}
