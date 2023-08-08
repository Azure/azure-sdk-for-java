// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Immutable;

/**
 * The message of disconnected.
 */
@Immutable
public final class DisconnectedMessage {

    private final String reason;

    /**
     * Creates a new instance of DisconnectedMessage.
     *
     * @param reason the reason of disconnect.
     */
    public DisconnectedMessage(String reason) {
        this.reason = reason;
    }

    /**
     * Gets the reason of disconnect.
     *
     * @return the reason of disconnect.
     */
    public String getReason() {
        return reason;
    }
}
