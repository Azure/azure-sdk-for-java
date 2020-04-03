// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;
/**
 * Type representing the lock-token. The lock token is a reference to the lock that is being held by the Service Bus in
 * {@link ReceiveMode#PEEK_LOCK} mode.
 */
public interface MessageLockToken {

    /**
     * Gets the lock token.
     *
     * @return Lock token for the message.
     */
    String getLockToken();

    /**
     * Created the {@link MessageLockToken} given a {@link String}.
     *
     * @param token {@link String} for creating {@link MessageLockToken} instance;
     *
     * @return The created {@link MessageLockToken} object.
     */
    static MessageLockToken fromToken(String token) {
        return () -> token;
    }
}
