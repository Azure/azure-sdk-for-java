// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a work item that can be scheduled multiple times.
 */
class RetriableWorkItem {
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final MonoSink<Void> monoSink;
    private final TimeoutTracker timeoutTracker;
    private final byte[] amqpMessage;
    private final int messageFormat;
    private final int encodedMessageSize;

    private boolean waitingForAck;
    private Exception lastKnownException;

    RetriableWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, MonoSink<Void> monoSink,
                      Duration timeout) {
        this(amqpMessage, encodedMessageSize, messageFormat, monoSink, new TimeoutTracker(timeout, false));
    }

    private RetriableWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, MonoSink<Void> monoSink,
                              TimeoutTracker timeout) {
        this.amqpMessage = amqpMessage;
        this.encodedMessageSize = encodedMessageSize;
        this.messageFormat = messageFormat;
        this.monoSink = monoSink;
        this.timeoutTracker = timeout;
    }

    byte[] getMessage() {
        return amqpMessage;
    }

    TimeoutTracker getTimeoutTracker() {
        return timeoutTracker;
    }

    MonoSink<Void> getSink() {
        return monoSink;
    }

    int incrementRetryAttempts() {
        return retryAttempts.incrementAndGet();
    }

    boolean hasBeenRetried() {
        return retryAttempts.get() == 0;
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

    void setWaitingForAck() {
        this.waitingForAck = true;
    }

    boolean isWaitingForAck() {
        return this.waitingForAck;
    }
}
