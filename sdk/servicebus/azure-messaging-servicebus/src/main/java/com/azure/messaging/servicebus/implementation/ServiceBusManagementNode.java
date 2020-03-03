// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;


/**
 * The management node for fetching metadata about the Service Bus and peek operation.
 */
public interface ServiceBusManagementNode extends AutoCloseable {
    /**
     * Completes a message given its lock token.
     * @return Mono that completes successfully when the message is completed. Otherwise, returns an error.
     */
    Mono<Void> updateDisposition(UUID lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify);

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
