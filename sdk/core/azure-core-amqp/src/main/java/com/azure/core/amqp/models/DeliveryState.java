// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * States for a message delivery.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-delivery-state">Delivery
 *     state</a>
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transactions-v1.0-os.html#section-txn-work">Transactional
 *     work</a>
 */
public class DeliveryState extends ExpandableStringEnum<DeliveryState> {
    /**
     * Indicates successful processing at the receiver.
     */
    public static final DeliveryState ACCEPTED = fromString("ACCEPTED", DeliveryState.class);
    /**
     * Indicates an invalid and unprocessable message.
     */
    public static final DeliveryState REJECTED = fromString("REJECTED", DeliveryState.class);
    /**
     * Indicates that the message was not (and will not be) processed.
     */
    public static final DeliveryState RELEASED = fromString("RELEASED", DeliveryState.class);
    /**
     * indicates that the message was modified, but not processed.
     */
    public static final DeliveryState MODIFIED = fromString("MODIFIED", DeliveryState.class);
    /**
     * indicates partial message data seen by the receiver as well as the starting point for a resumed transfer.
     */
    public static final DeliveryState RECEIVED = fromString("RECEIVED", DeliveryState.class);
    /**
     * Indicates that this delivery is part of a transaction.
     */
    public static final DeliveryState TRANSACTIONAL = fromString("TRANSACTIONAL", DeliveryState.class);
}
