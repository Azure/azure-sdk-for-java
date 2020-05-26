// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpTransaction;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.MonoSink;

import java.time.Duration;

/**
 * Represents a work item that can be scheduled multiple times.
 */
final class RetriableWorkItem extends WorkItem {

    private final MonoSink<Void> monoSink;

    RetriableWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, MonoSink<Void> monoSink,
        Duration timeout, AmqpTransaction transactionId) {

        super(amqpMessage, encodedMessageSize, messageFormat, timeout, transactionId);
        this.monoSink = monoSink;
    }

    @Override
    void success(DeliveryState deliveryState) {
        monoSink.success();
    }

    @Override
    void error(Throwable error) {
        monoSink.error(error);
    }

}
