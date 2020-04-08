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
    private final boolean enableSession;
    private final String sessionId;

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount) {
        this(receiveMode, prefetchCount, false, null);
    }

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount, boolean enableSession, String sessionId) {
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
        this.enableSession = enableSession;
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
     * @return the sessionId for the message.
     */
    String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the enableSession flag for the message.
     * @return the enableSession flag for the message.
     */
    boolean isSessionEnabled() {
        return enableSession;
    }

    int getPrefetchCount() {
        return prefetchCount;
    }
}
