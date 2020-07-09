// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.TracerProvider;

/**
 * A class for aggregating {@link EventData} into a single, size-limited, batch. It is treated as a single message when
 * sent to the Azure Event Hubs service.
 *
 * @see EventHubProducerClient#createBatch()
 * @see EventHubProducerAsyncClient#createBatch()
 * @see EventHubClientBuilder See EventHubClientBuilder for examples of building an asynchronous or synchronous
 *     producer.
 */
public final class EventDataBatch extends EventDataBatchBase {
    EventDataBatch(int maxMessageSize, String partitionId, String partitionKey, ErrorContextProvider contextProvider,
        TracerProvider tracerProvider, String entityPath, String hostname) {
        super(maxMessageSize, partitionId, partitionKey, contextProvider, tracerProvider, entityPath, hostname);
    }

    /**
     * Tries to add an {@link EventData event} to the batch.
     *
     * @param eventData The {@link EventData} to add to the batch.
     * @return {@code true} if the event could be added to the batch; {@code false} if the event was too large to fit in
     *     the batch.
     * @throws IllegalArgumentException if {@code eventData} is {@code null}.
     * @throws AmqpException if {@code eventData} is larger than the maximum size of the {@link EventDataBatch}.
     */
    public boolean tryAdd(final EventData eventData) {
        return super.tryAdd(eventData);
    }
}
