// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.UUID;

/**
 * Represents a received message containing a lock token.
 */
public class MessageWithLockToken extends MessageImpl {
    private final UUID lockToken;

    MessageWithLockToken(Message message, UUID lockToken) {
        super(message.getHeader(), message.getDeliveryAnnotations(), message.getMessageAnnotations(),
            message.getProperties(), message.getApplicationProperties(), message.getBody(), message.getFooter());
        this.lockToken = lockToken;
    }

    /**
     * Gets the lock token associated with this message.
     *
     * @return The lock token associated with this message or {@link MessageUtils#ZERO_LOCK_TOKEN} if there is none.
     */
    public UUID getLockToken() {
        return lockToken;
    }
}
