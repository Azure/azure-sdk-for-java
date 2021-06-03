// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * States for a message delivery.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-delivery-state">Delivery
 *     state</a>
 */
public enum DeliveryState {
    // indicates successful processing at the receiver.
    ACCEPTED,
    // indicates an invalid and unprocessable message.
    REJECTED,
    // indicates that the message was not (and will not be) processed.
    RELEASED,
    // indicates that the message was modified, but not processed.
    MODIFIED,
    // indicates partial message data seen by the receiver as well as the starting point for a resumed transfer.
    RECEIVED,
}
