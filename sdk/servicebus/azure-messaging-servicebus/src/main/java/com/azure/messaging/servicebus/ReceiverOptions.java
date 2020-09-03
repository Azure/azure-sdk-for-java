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
    private final boolean isRollingSessionReceiver;
    private final Integer maxConcurrentSessions;
    private final boolean isSessionReceiver;

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount) {
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
        this.sessionId = null;
        this.isRollingSessionReceiver = false;
        this.maxConcurrentSessions = null;
        this.isSessionReceiver = false;
    }

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount,
        String sessionId, boolean isRollingSessionReceiver, Integer maxConcurrentSessions) {
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
        this.sessionId = sessionId;
        this.isRollingSessionReceiver = isRollingSessionReceiver;
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.isSessionReceiver = true;
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

    /**
     * Gets the number of messages to prefetch.
     *
     * @return The number of messages to prefetch.
     */
    int getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * Gets whether or not the receiver is a session-aware receiver.
     *
     * @return true if it is a session-aware receiver; false otherwise.
     */
    boolean isSessionReceiver() {
        return isSessionReceiver;
    }

    /**
     * Gets whether or not this receiver should roll over when a session has completed processing.
     *
     * @return {@code true} if this receiver should roll over to next session when it has completed processing; {@code
     *     false} otherwise.
     */
    public boolean isRollingSessionReceiver() {
        return isRollingSessionReceiver;
    }

    /**
     * Gets the maximum number of concurrent sessions.
     *
     * @return The maximum number of concurrent sessions to process.
     */
    public Integer getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }
}
