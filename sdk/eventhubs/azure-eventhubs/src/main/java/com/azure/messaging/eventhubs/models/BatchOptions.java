// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducer;

/**
 * The set of options that can be specified when creating an {@link EventDataBatch}.
 *
 * @see EventHubProducer#createBatch()
 * @see EventHubProducer#createBatch(BatchOptions)
 */
public class BatchOptions implements Cloneable {
    private int maximumSizeInBytes;
    private String partitionKey;

    /**
     * Sets the maximum size for the {@link EventDataBatch batch of events}, in bytes.
     *
     * @param maximumSizeInBytes The maximum size to allow for the {@link EventDataBatch batch of events}.
     * @return The updated {@link BatchOptions} object.
     */
    public BatchOptions maximumSizeInBytes(int maximumSizeInBytes) {
        this.maximumSizeInBytes = maximumSizeInBytes;
        return this;
    }

    /**
     * Gets the maximum size to allow for the batch of events, in bytes.
     *
     * @return The maximum size to allow for a single batch of events, in bytes.
     */
    public int maximumSizeInBytes() {
        return maximumSizeInBytes;
    }

    /**
     * Sets a hashing key to be provided for the batch of events. Events with the same {@code partitionKey} are hashed
     * and sent to the same partition.
     *
     * @param partitionKey The partition hashing key to associate with the event or batch of events.
     * @return The updated {@link BatchOptions} object.
     */
    public BatchOptions partitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    /**
     * Gets the partition routing key on an event batch. If specified, tells the Event Hubs service that these events
     * belong to the same group and should belong to the same partition.
     *
     * @return The partition hashing key to associate with the event or batch of events.
     */
    public String partitionKey() {
        return partitionKey;
    }

    /**
     * Creates a shallow clone of this instance.
     *
     * @return A shallow clone of this object.
     */
    @Override
    public BatchOptions clone() {
        BatchOptions clone;
        try {
            clone = (BatchOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new BatchOptions();
        }

        return clone.partitionKey(partitionKey)
            .maximumSizeInBytes(maximumSizeInBytes);
    }
}
