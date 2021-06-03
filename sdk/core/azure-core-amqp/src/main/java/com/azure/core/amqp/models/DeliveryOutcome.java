// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.annotation.Fluent;

/**
 * There are different outcomes accepted by the AMQP protocol layer.
 *
 * Outcomes that don't have any other fields
 * http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-accepted
 * http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-released
 */
@Fluent
public class DeliveryOutcome {
    private DeliveryState deliveryState;

    /**
     * Creates an instance of the delivery outcome with its state.
     *
     * @param deliveryState The state of the delivery.
     */
    public DeliveryOutcome(DeliveryState deliveryState) {
        this.deliveryState = deliveryState;
    }

    /**
     * Gets the delivery state.
     *
     * @return The delivery state.
     */
    public DeliveryState getDeliveryState() {
        return deliveryState;
    }

    /**
     * Sets the delivery state.
     *
     * @param deliveryState The delivery state.
     */
    void setDeliveryState(DeliveryState deliveryState) {
        this.deliveryState = deliveryState;
    }
}
