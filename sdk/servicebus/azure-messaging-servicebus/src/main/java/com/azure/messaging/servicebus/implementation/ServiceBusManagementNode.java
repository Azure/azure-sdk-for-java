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
     *
     * @return Flux of ReceivedMessage.
     */
    Mono<ServiceBusReceivedMessage> peek();

    @Override
    void close();
}
