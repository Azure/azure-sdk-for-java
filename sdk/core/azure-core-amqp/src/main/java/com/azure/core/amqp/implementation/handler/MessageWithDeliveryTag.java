// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.UUID;

/**
 * Represents a received message containing a delivery tag.
 */
public final class MessageWithDeliveryTag extends MessageImpl {
    private final UUID deliveryTag;

    MessageWithDeliveryTag(Message message, UUID deliveryTag) {
        super(message.getHeader(), message.getDeliveryAnnotations(), message.getMessageAnnotations(),
            message.getProperties(), message.getApplicationProperties(), message.getBody(), message.getFooter());
        this.deliveryTag = deliveryTag;
    }

    /**
     * Gets the delivery tag associated with this message.
     *
     * @return The delivery tag associated with this message.
     */
    public UUID getDeliveryTag() {
        return deliveryTag;
    }
}
