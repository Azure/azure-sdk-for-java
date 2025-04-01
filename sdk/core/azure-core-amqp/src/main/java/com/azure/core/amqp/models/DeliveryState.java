// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import io.clientcore.core.utils.ExpandableEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * States for a message delivery.
 *
 * @see <a href=
 * "http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-delivery-state">Delivery
 * state</a>
 * @see <a href=
 * "http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transactions-v1.0-os.html#section-txn-work">Transactional
 * work</a>
 */
public final class DeliveryState implements ExpandableEnum<String> {
    private static final Map<String, DeliveryState> VALUES = new ConcurrentHashMap<>();
    private final String state;

    /**
     * Indicates successful processing at the receiver.
     */
    public static final DeliveryState ACCEPTED = fromString("ACCEPTED");
    /**
     * Indicates an invalid and unprocessable message.
     */
    public static final DeliveryState REJECTED = fromString("REJECTED");
    /**
     * Indicates that the message was not (and will not be) processed.
     */
    public static final DeliveryState RELEASED = fromString("RELEASED");
    /**
     * indicates that the message was modified, but not processed.
     */
    public static final DeliveryState MODIFIED = fromString("MODIFIED");
    /**
     * indicates partial message data seen by the receiver as well as the starting point for a resumed transfer.
     */
    public static final DeliveryState RECEIVED = fromString("RECEIVED");
    /**
     * Indicates that this delivery is part of a transaction.
     */
    public static final DeliveryState TRANSACTIONAL = fromString("TRANSACTIONAL");

    private DeliveryState(String state) {
        this.state = state;
    }

    /**
     * Creates or finds an DeliveryState from its string representation.
     *
     * @param state the state to look for
     * @return the corresponding DeliveryState
     */
    public static DeliveryState fromString(String state) {
        if (state == null) {
            return null;
        }
        return VALUES.computeIfAbsent(state, DeliveryState::new);
    }

    @Override
    public String getValue() {
        return this.state;
    }

    @Override
    public String toString() {
        return this.state;
    }
}
