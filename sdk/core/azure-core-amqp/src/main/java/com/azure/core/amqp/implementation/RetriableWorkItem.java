// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.apache.qpid.proton.engine.Sender;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a work item that can be scheduled multiple times.
 */
class RetriableWorkItem {
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final MonoSink<DeliveryState> monoSink;
    private final TimeoutTracker timeoutTracker;
    private final ReadableBuffer encodedBuffer;
    private final byte[] encodedBytes;
    private final int messageFormat;
    private final int encodedMessageSize;
    private final DeliveryState deliveryState;
    private boolean waitingForAck;
    private Exception lastKnownException;

    private final AmqpMetricsProvider metricsProvider;
    private long tryStartTime = 0;

    RetriableWorkItem(ReadableBuffer buffer, int messageFormat, MonoSink<DeliveryState> monoSink, Duration timeout,
        DeliveryState deliveryState, AmqpMetricsProvider metricsProvider) {
        this.encodedBuffer = buffer;
        this.encodedBytes = null;
        this.encodedMessageSize = buffer.remaining();
        this.messageFormat = messageFormat;
        this.monoSink = monoSink;
        this.timeoutTracker = new TimeoutTracker(timeout, false);
        this.deliveryState = deliveryState;
        this.metricsProvider = metricsProvider;
    }

    RetriableWorkItem(byte[] bytes, int encodedMessageSize, int messageFormat, MonoSink<DeliveryState> monoSink,
        Duration timeout, DeliveryState deliveryState, AmqpMetricsProvider metricsProvider) {
        this.encodedBytes = bytes;
        this.encodedBuffer = null;
        this.encodedMessageSize = encodedMessageSize;
        this.messageFormat = messageFormat;
        this.monoSink = monoSink;
        this.timeoutTracker = new TimeoutTracker(timeout, false);
        this.deliveryState = deliveryState;
        this.metricsProvider = metricsProvider;
    }

    DeliveryState getDeliveryState() {
        return deliveryState;
    }

    boolean isDeliveryStateProvided() {
        return deliveryState != null;
    }

    TimeoutTracker getTimeoutTracker() {
        return timeoutTracker;
    }

    void success(DeliveryState deliveryState) {
        reportMetrics(deliveryState);
        monoSink.success(deliveryState);
    }

    void error(Throwable error, DeliveryState deliveryState) {
        reportMetrics(deliveryState);
        monoSink.error(error);
    }

    int incrementRetryAttempts() {
        return retryAttempts.incrementAndGet();
    }

    void beforeTry() {
        if (metricsProvider.isSendDeliveryEnabled()) {
            this.tryStartTime = Instant.now().toEpochMilli();
        }
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

    void send(Sender sender) {
        final int sentMsgSize;
        if (encodedBytes != null) {
            sentMsgSize = sender.send(encodedBytes, 0, encodedMessageSize);
        } else {
            encodedBuffer.rewind();
            sentMsgSize = sender.send(encodedBuffer);
        }
        assert sentMsgSize == encodedMessageSize : "Contract of the ProtonJ library for Sender. Send API changed";
    }

    private void reportMetrics(DeliveryState deliveryState) {
        if (metricsProvider.isSendDeliveryEnabled()) {
            metricsProvider.recordSend(tryStartTime, deliveryState == null ? null : deliveryState.getType());
        }
    }
}
