// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.amqp.transport.DeliveryState;
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
    private final byte[] amqpMessage;
    private final int messageFormat;
    private final int encodedMessageSize;
    private final DeliveryState deliveryState;
    private boolean waitingForAck;
    private Exception lastKnownException;

    private final AmqpMetricsProvider metricsProvider;
    private long tryStartTime = 0;

    RetriableWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, MonoSink<DeliveryState> monoSink,
                      Duration timeout, DeliveryState deliveryState, AmqpMetricsProvider metricsProvider) {
        this(amqpMessage, encodedMessageSize, messageFormat, monoSink, new TimeoutTracker(timeout,
            false), deliveryState, metricsProvider);
    }

    private RetriableWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, MonoSink<DeliveryState>
        monoSink, TimeoutTracker timeout, DeliveryState deliveryState, AmqpMetricsProvider metricsProvider) {
        this.amqpMessage = amqpMessage;
        this.encodedMessageSize = encodedMessageSize;
        this.messageFormat = messageFormat;
        this.monoSink = monoSink;
        this.timeoutTracker = timeout;
        this.deliveryState = deliveryState;
        this.metricsProvider = metricsProvider;
    }

    byte[] getMessage() {
        return amqpMessage;
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

    private void reportMetrics(DeliveryState deliveryState) {
        if (metricsProvider.isSendDeliveryEnabled()) {
            metricsProvider.recordSend(tryStartTime, deliveryState == null ? null : deliveryState.getType());
        }
    }
}
