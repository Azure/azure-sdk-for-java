// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

import java.util.Objects;
import java.util.UUID;

import static com.azure.messaging.servicebus.implementation.Messages.INVALID_LOCK_TOKEN_STRING;

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
     * @param lockToken {@link String} for creating {@link MessageLockToken} instance;
     *
     * @return The created {@link MessageLockToken} object.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws IllegalArgumentException if {@code lockToken} is empty or invalid.
     */
    static MessageLockToken fromString(String lockToken) {
        return () -> {
            if (Objects.isNull(lockToken)) {
                throw new NullPointerException("'lockToken' cannot be null.");
            } else if (lockToken.isEmpty()) {
                throw new IllegalArgumentException("'lockToken' cannot be empty.");
            }

            try {
                UUID.fromString(lockToken);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException(String.format(INVALID_LOCK_TOKEN_STRING, lockToken));
            }
            return lockToken;
        };
    }
}
