// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ReplayableWorkItem<T> extends WorkItem<T> {
    private byte[] amqpMessage;
    private int messageFormat;
    private int encodedMessageSize;
    private boolean waitingForAck;

    private Exception lastKnownException;
    private CompletableFuture<?> timeoutTask;

    public ReplayableWorkItem(final byte[] amqpMessage, final int encodedMessageSize, final int messageFormat, final CompletableFuture<T> completableFuture, final Duration timeout) {
        super(completableFuture, timeout);
        this.initialize(amqpMessage, encodedMessageSize, messageFormat);
    }

    public ReplayableWorkItem(final byte[] amqpMessage, final int encodedMessageSize, final int messageFormat, final CompletableFuture<T> completableFuture, final TimeoutTracker timeout) {
        super(completableFuture, timeout);
        this.initialize(amqpMessage, encodedMessageSize, messageFormat);
    }

    private void initialize(final byte[] amqpMessage, final int encodedMessageSize, final int messageFormat) {
        this.amqpMessage = amqpMessage;
        this.messageFormat = messageFormat;
        this.encodedMessageSize = encodedMessageSize;
    }

    public byte[] getMessage() {
        return this.amqpMessage;
    }

    public void clearMessage() {
        this.amqpMessage = null;
    }

    public int getEncodedMessageSize() {
        return this.encodedMessageSize;
    }

    public int getMessageFormat() {
        return this.messageFormat;
    }

    public Exception getLastKnownException() {
        return this.lastKnownException;
    }

    public void setLastKnownException(Exception exception) {
        this.lastKnownException = exception;
    }

    public CompletableFuture<?> getTimeoutTask() {
        return this.timeoutTask;
    }

    public void setTimeoutTask(final CompletableFuture<?> timeoutTask) {
        this.timeoutTask = timeoutTask;
    }

    public void setWaitingForAck() {
        this.waitingForAck = true;
    }

    public boolean isWaitingForAck() {
        return this.waitingForAck;
    }
}
