// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.nio.ByteBuffer;

/**
 * Keep additional information for example delivery tag which represent lock token.
 */
public class ExtendedAmqpMessage extends MessageImpl {

    private final ByteBuffer deliveryTag;

    /**
     * Constructor
     * @param message The message.
     * @param deliveryTag delivery tag.
     */
    public ExtendedAmqpMessage(Message message, ByteBuffer deliveryTag) {
        super(message.getHeader(), message.getDeliveryAnnotations(), message.getMessageAnnotations(),
            message.getProperties(), message.getApplicationProperties(),
            message.getBody(), message.getFooter());
        this.deliveryTag = deliveryTag;
    }

    /**
     * Get delivery tag
     * @return {@link ByteBuffer} representing delivery tag.
     */
    public ByteBuffer getDeliveryTag() {
        return this.deliveryTag;
    }

}
