// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.util.UUID;

/**
 * Type representing the lock-token.
 */
public interface MessageLockToken {

    /**
     * Gets the lock token.
     *
     * @return {@link UUID} representing the lock-token.
     */
    UUID getLockToken();

    /**
     * Created the {@link MessageLockToken} given a {@link UUID}.
     *
     * @param uuid to use for creating {@link MessageLockToken} instance;
     *
     * @return The created {@link MessageLockToken} object.
     */
    static MessageLockToken fromUuid(UUID uuid) {
        return () -> uuid;
    }
}
