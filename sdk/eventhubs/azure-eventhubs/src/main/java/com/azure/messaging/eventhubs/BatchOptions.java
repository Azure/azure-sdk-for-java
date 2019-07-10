// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

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
     * Sets a hashing key to be provided for the batch of events, which instructs the Event Hubs service map this key to
     * a specific partition but allowing the service to choose an arbitrary, partition for this batch of events and any
     * other batches using the same partition hashing key.
     *
     * The selection of a partition is stable for a given partition hashing key. Should any other batches of events be
     * sent using the same exact partition hashing key, the Event Hubs service will route them all to the same
     * partition.
     *
     * This should be specified only when there is a need to group events by partition, but there is flexibility into
     * which partition they are routed. If ensuring that a batch of events is sent only to a specific partition, it is
     * recommended that the identifier of the position be specified directly when sending the batch.
     *
     * @param partitionKey The partition hashing key to associate with the event or batch of events.
     * @return The updated {@link SendOptions} object.
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
     * Creates a shallow clone of this instance. The parameters are not cloned, but they are immutable.
     *
     * @return A shallow clone of this object.
     */
    @Override
    public Object clone() {
        BatchOptions clone;
        try {
            clone = (BatchOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new BatchOptions();
        }

        clone.partitionKey(partitionKey);
        clone.maximumSizeInBytes(maximumSizeInBytes);

        return clone;
    }
}
