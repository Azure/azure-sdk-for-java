// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.message.Message;

import java.util.UUID;

/**
 * Holds loks token for AMQP message.
 */
public class AmqpMessageWithLockToken {
    private final Message message;
    private final UUID lockToken;

    /***
     *
     * @param message The amqp message
     * @param lockToken lockToken
     */
    public AmqpMessageWithLockToken(Message message, UUID lockToken) {
        this.message = message;
        this.lockToken = lockToken;
    }

    /**
     *
     * @return  the amqp message
     */
    public Message getMessage() {
        return message;
    }

    /**
     *
     * @return the lock token.
     */
    public UUID getLockToken() {
        return lockToken;
    }
}
