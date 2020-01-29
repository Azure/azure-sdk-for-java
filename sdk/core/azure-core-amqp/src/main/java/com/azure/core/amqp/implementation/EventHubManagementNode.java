package com.azure.core.amqp.implementation;

import reactor.core.publisher.Mono;

public interface EventHubManagementNode extends AutoCloseable {
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
