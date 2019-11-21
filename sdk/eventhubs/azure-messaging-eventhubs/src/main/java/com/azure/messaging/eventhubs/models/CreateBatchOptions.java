// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;

/**
 * The set of options that can be specified when creating an {@link EventDataBatch}.
 *
 * @see EventHubProducerClient#createBatch(CreateBatchOptions)
 * @see EventHubProducerAsyncClient#createBatch(CreateBatchOptions)
 */
@Fluent
public class CreateBatchOptions {
    private int maximumSizeInBytes;
    private String partitionKey;
    private String partitionId;

    /**
     * Sets the maximum size for the {@link EventDataBatch batch of events}, in bytes.
     *
     * @param maximumSizeInBytes The maximum size to allow for the {@link EventDataBatch batch of events}.
     *
     * @return The updated {@link CreateBatchOptions} object.
     */
    public CreateBatchOptions setMaximumSizeInBytes(int maximumSizeInBytes) {
        this.maximumSizeInBytes = maximumSizeInBytes;
        return this;
    }

    /**
     * Gets the maximum size to allow for the batch of events, in bytes.
     *
     * @return The maximum size to allow for a single batch of events, in bytes.
     */
    public int getMaximumSizeInBytes() {
        return maximumSizeInBytes;
    }

    /**
     * Sets a hashing key to be provided for the batch of events. Events with the same {@code partitionKey} are hashed
     * and sent to the same partition.
     *
     * @param partitionKey The partition hashing key to associate with the event or batch of events.
     *
     * @return The updated {@link CreateBatchOptions} object.
     */
    public CreateBatchOptions setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    /**
     * Gets the partition routing key on an event batch. If specified, tells the Event Hubs service that these events
     * belong to the same group and should belong to the same partition.
     *
     * @return The partition hashing key to associate with the event or batch of events.
     */
    public String getPartitionKey() {
        return partitionKey;
    }

    /**
     * Gets the identifier of the Event Hub partition that the events in the {@link EventDataBatch} will be sent to. If
     * the identifier is not specified, the Event Hubs service will be responsible for routing events that are sent to
     * an available partition.
     *
     * @return The identifier of the Event Hub partition that the {@link EventDataBatch} will be set to. {@code null} or
     *     an empty string if Event Hubs service is responsible for routing events.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Sets the identifier of the Event Hub partition that the events in the {@link EventDataBatch} will be sent to. If
     * the identifier is not specified, the Event Hubs service will be responsible for routing events that are sent to
     * an available partition.
     *
     * @param partitionId The identifier of the Event Hub partition that the {@link EventDataBatch batch's} events
     *     will be sent to. {@code null} or an empty string if Event Hubs service is responsible for routing events.
     *
     * @return The updated {@link CreateBatchOptions} object.
     */
    public CreateBatchOptions setPartitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }
}
