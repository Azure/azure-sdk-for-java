// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class RetriableResponseWorkItem extends WorkItem {

    private final ClientLogger logger = new ClientLogger(RetriableResponseWorkItem.class);

    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final MonoSink<DeliveryState> monoSink;
    private final TimeoutTracker timeoutTracker;
    private final byte[] amqpMessage;
    private final int messageFormat;
    private final int encodedMessageSize;

    private boolean waitingForAck;
    private Exception lastKnownException;

    RetriableResponseWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat,
        MonoSink<DeliveryState> monoSink, Duration timeout) {
        this(amqpMessage, encodedMessageSize, messageFormat, monoSink, new TimeoutTracker(timeout,
            false));
    }

    private RetriableResponseWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat,
        MonoSink<DeliveryState> monoSink, TimeoutTracker timeout) {

        this.amqpMessage = amqpMessage;
        this.encodedMessageSize = encodedMessageSize;
        this.messageFormat = messageFormat;
        this.monoSink = monoSink;
        this.timeoutTracker = timeout;
    }

    byte[] getMessage() {
        return amqpMessage;
    }

    @Override
    TimeoutTracker getTimeoutTracker() {
        return timeoutTracker;
    }

    @Override
    void success(DeliveryState deliveryState) {
        logger.verbose(" !!!! Calling success with [{}].", deliveryState);
        monoSink.success(deliveryState);
    }

    @Override
    void error(Throwable error) {
        monoSink.error(error);
    }

    int incrementRetryAttempts() {
        return retryAttempts.incrementAndGet();
    }

    @Override
    boolean hasBeenRetried() {
        return retryAttempts.get() == 0;
    }

    @Override
    int getEncodedMessageSize() {
        return encodedMessageSize;
    }

    @Override
    int getMessageFormat() {
        return messageFormat;
    }

    Exception getLastKnownException() {
        return this.lastKnownException;
    }

    @Override
    void setLastKnownException(Exception exception) {
        this.lastKnownException = exception;
    }

    @Override
    void setWaitingForAck() {
        this.waitingForAck = true;
    }

    boolean isWaitingForAck() {
        return this.waitingForAck;
    }
}
