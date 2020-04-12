// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

/**
 * Options set when creating a service bus receiver.
 */
class ReceiverOptions {
    private final ReceiveMode receiveMode;
    private final int prefetchCount;
    private final String sessionId;

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount, String sessionId) {
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
        this.sessionId = sessionId;
    }

    /**
     * Gets the receive mode for the message.
     *
     * @return the receive mode for the message.
     */
    ReceiveMode getReceiveMode() {
        return receiveMode;
    }

    /**
     * Gets the sessionId for the message.
     *
     * @return Id of the session to receive messages from.
     */
    String getSessionId() {
        return sessionId;
    }

    int getPrefetchCount() {
        return prefetchCount;
    }
}
