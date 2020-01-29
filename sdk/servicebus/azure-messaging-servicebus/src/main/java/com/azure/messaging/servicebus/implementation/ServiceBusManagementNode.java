package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.EventHubProperties;
import com.azure.core.amqp.implementation.PartitionProperties;
import reactor.core.publisher.Mono;

public interface ServiceBusManagementNode extends AutoCloseable {
    /**
     * Gets the metadata associated with the Event Hub.
     *
     * @return Metadata associated with the Event Hub.
     */
    Mono<EventHubProperties> getEventHubProperties();

    /**
     * Gets the metadata associated with a particular partition in the Event Hub.
     *
     * @param partitionId The identifier of the partition.
     * @return The metadata associated with the partition.
     */
    Mono<PartitionProperties> getPartitionProperties(String partitionId);

    @Override
    void close();
}
