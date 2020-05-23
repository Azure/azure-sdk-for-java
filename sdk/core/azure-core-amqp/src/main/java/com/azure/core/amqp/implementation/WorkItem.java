// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.amqp.transport.DeliveryState;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is abstract class and any class extending this would be able to handle success and error based on their needs.
 *
 */
abstract class WorkItem {
    final AtomicInteger retryAttempts = new AtomicInteger();
    private boolean waitingForAck;
    private final int messageFormat;
    private final TimeoutTracker timeoutTracker;
    private final byte[] amqpMessage;
    private final int encodedMessageSize;
    private final ByteBuffer transactionId;

    private Exception lastKnownException;

    WorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, Duration timeout,
        ByteBuffer transactionId) {
        this.amqpMessage = amqpMessage;
        this.encodedMessageSize = encodedMessageSize;
        this.messageFormat = messageFormat;
        this.timeoutTracker = new TimeoutTracker(timeout, false);
        this.transactionId = transactionId;
    }

    abstract void success(DeliveryState delivery);
    abstract void error(Throwable error);

    byte[] getMessage() {
        return amqpMessage;
    }

    ByteBuffer getTransactionId() {
        return transactionId;
    }

    TimeoutTracker getTimeoutTracker() {
        return timeoutTracker;
    }

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

    int getEncodedMessageSize() {
        return encodedMessageSize;
    }

    int getMessageFormat() {
        return messageFormat;
    }

    Exception getLastKnownException() {
        return this.lastKnownException;
    }

    void setLastKnownException(Exception exception) {
        this.lastKnownException = exception;
    }
}
