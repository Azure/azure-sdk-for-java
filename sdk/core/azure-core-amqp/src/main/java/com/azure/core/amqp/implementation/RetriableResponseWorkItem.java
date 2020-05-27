// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.MonoSink;

import java.time.Duration;

/**
 * Work item which returns {@link DeliveryState} to subscriber and that can be scheduled multiple times..
 */
final class RetriableResponseWorkItem extends WorkItem {

    private final MonoSink<DeliveryState> monoSink;

    RetriableResponseWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat,
        MonoSink<DeliveryState> monoSink, Duration timeout) {

        super(amqpMessage, encodedMessageSize, messageFormat, timeout, null);
        this.monoSink = monoSink;
    }

    @Override
    void success(DeliveryState deliveryState) {
        monoSink.success(deliveryState);
    }

    @Override
    void error(Throwable error) {
        monoSink.error(error);
    }

}
