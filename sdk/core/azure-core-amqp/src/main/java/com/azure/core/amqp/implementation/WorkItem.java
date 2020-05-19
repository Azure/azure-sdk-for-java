// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.amqp.transport.DeliveryState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is abstract class and any class extending this would be able to handle success and error based on their needs.
 *
 */
public abstract class WorkItem {
    final AtomicInteger retryAttempts = new AtomicInteger();
    private boolean waitingForAck;

    abstract int getMessageFormat();
    abstract byte[] getMessage();
    abstract int getEncodedMessageSize();
    abstract void success(DeliveryState delivery);
    abstract void error(Throwable error);
    abstract TimeoutTracker getTimeoutTracker();
    abstract void setLastKnownException(Exception exception);

    boolean hasBeenRetried() {
        return retryAttempts.get() == 0;
    }

    int incrementRetryAttempts() {
        return retryAttempts.incrementAndGet();
    }

    void setWaitingForAck() {
        this.waitingForAck = true;
    }

    boolean isWaitingForAck() {
        return this.waitingForAck;
    }
}
