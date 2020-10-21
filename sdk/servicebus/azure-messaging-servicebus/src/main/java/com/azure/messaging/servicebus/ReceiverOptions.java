// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

import java.time.Duration;

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
    private final Duration maxLockRenewDuration;

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount, Duration maxLockRenewDuration) {
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
        this.maxLockRenewDuration = maxLockRenewDuration;
        this.sessionId = null;
        this.isRollingSessionReceiver = false;
        this.maxConcurrentSessions = null;
        this.isSessionReceiver = false;
    }

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount, String sessionId, boolean isRollingSessionReceiver,
        Integer maxConcurrentSessions, Duration maxLockRenewDuration) {
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
        this.sessionId = sessionId;
        this.isRollingSessionReceiver = isRollingSessionReceiver;
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.maxLockRenewDuration = maxLockRenewDuration;
        this.isSessionReceiver = true;
    }

    /**
     * Gets the {@code maxLockRenewDuration} for the message lock or session lock.
     *
     * @return the max lock duration for the message lock or session lock.
     */
    Duration getMaxLockRenewDuration() {
        return maxLockRenewDuration;
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
     * Determine if client have enabled auto renew of message or session lock.
     *
     * @return true if  autoRenew is enabled; false otherwise.
     */
    boolean isAutoLockRenewEnabled() {
        return maxLockRenewDuration != null && !maxLockRenewDuration.isZero() && !maxLockRenewDuration.isNegative();
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
