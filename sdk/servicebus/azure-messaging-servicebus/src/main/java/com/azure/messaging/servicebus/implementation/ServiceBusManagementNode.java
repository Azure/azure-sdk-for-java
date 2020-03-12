// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

import reactor.core.publisher.Mono;


/**
 * The management node for fetching metadata about the Service Bus and peek operation.
 */
public interface ServiceBusManagementNode extends AutoCloseable {

    /**
     * This will return next available message to peek.
     * @return {@link Mono} of {@link ServiceBusReceivedMessage}.
     */
    Mono<ServiceBusReceivedMessage> peek();

    /**
     * @param fromSequenceNumber to peek message from.
     * @return {@link Mono} of {@link ServiceBusReceivedMessage}.
     */
    Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber);

    @Override
    void close();
}
