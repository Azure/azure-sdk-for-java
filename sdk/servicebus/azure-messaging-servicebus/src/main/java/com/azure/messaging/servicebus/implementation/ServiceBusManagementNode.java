// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Flux;

/**
 * The management node for fetching metadata about the Service Bus and peek operation.
 */
public interface ServiceBusManagementNode extends AutoCloseable {


    /**
     *
     * @param maxMessages to be returned
     * @return Flux of ReceivedMessage.
     */
    Flux<ServiceBusReceivedMessage> peek(int maxMessages);

    /**
     *
     * @param maxMessages to be returned
     * @param sequenceNumber from where to start peeking.
     * @return Flux of ReceivedMessage.
     */
    Flux<ServiceBusReceivedMessage> peek(int maxMessages, int sequenceNumber);

    @Override
    void close();
}
