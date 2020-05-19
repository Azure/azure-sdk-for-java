// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a work item that can be scheduled multiple times.
 */
class RetriableWorkItem extends WorkItem {

    private final ClientLogger logger = new ClientLogger(RetriableWorkItem.class);

    private final MonoSink<Void> monoSink;
    private final TimeoutTracker timeoutTracker;
    private final byte[] amqpMessage;
    private final int messageFormat;
    private final int encodedMessageSize;

    private Exception lastKnownException;

    RetriableWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, MonoSink<Void> monoSink,
                      Duration timeout) {
        this(amqpMessage, encodedMessageSize, messageFormat, monoSink, new TimeoutTracker(timeout,
            false));
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

    @Override
    TimeoutTracker getTimeoutTracker() {
        return timeoutTracker;
    }

    @Override
    void success(DeliveryState deliveryState) {
        logger.verbose(" !!!! Calling success.");
        monoSink.success();
    }

    @Override
    void error(Throwable error) {
        monoSink.error(error);
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

}
