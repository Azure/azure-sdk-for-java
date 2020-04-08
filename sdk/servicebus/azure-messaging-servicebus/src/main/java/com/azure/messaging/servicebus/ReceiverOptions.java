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
    private boolean enableSession;
    private String sessionId;

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount) {
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
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
     * Update enableSession flag.
     * @param isSessionEnabled {@code true} if sessions are enabled; {@code false} otherwise.
     *
     * @return updated {@link ReceiverOptions} .
     */
    ReceiverOptions setEnableSession(boolean isSessionEnabled) {
        this.enableSession = isSessionEnabled;
        return this;
    }

    /**
     * Update sessionId.
     *
     * @return updated {@link ReceiverOptions} .
     */
    ReceiverOptions setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
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
