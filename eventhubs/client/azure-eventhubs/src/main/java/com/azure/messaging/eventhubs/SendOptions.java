// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

/**
 * The set of options that can be specified when sending a set of events to configure how the event data is packaged
 * into batches.
 */
public class SendOptions {
    private int maximumSizeInBytes;
    private String partitionKey;

    /**
     * Creates an instance with the maximum message size set to the maximum amount allowed by the protocol.
     */
    public SendOptions() {
        this.maximumSizeInBytes = EventHubProducer.MAX_MESSAGE_LENGTH_BYTES;
    }

    /**
     * Sets the maximum size to allow for a single batch of events, in bytes. If this size is exceeded, an exception
     * will be thrown and the send operation will fail.
     *
     * @param maximumSizeInBytes The maximum size to allow for a single batch of events.
     * @return The updated EventBatchingOptions object.
     */
    SendOptions maximumSizeInBytes(int maximumSizeInBytes) {
        this.maximumSizeInBytes = maximumSizeInBytes;
        return this;
    }

    /**
     * Gets the maximum size to allow for a single batch of events, in bytes. If this size is exceeded, an exception
     * will be thrown and the send operation will fail.
     *
     * @return The maximum size to allow for a single batch of events, in bytes.
     */
    int maximumSizeInBytes() {
        return maximumSizeInBytes;
    }

    /**
     * Sets a partition key on an event batch, which tells the Event Hubs service to send all events with that partition
     * routing key to the same partition.
     *
     * @param partitionKey The label of an event batch.
     * @return The updated EventBatchingOptions object.
     */
    public SendOptions partitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    /**
     * Gets the partition routing key on an event batch. If specified, tells the Event Hubs service that these events
     * belong to the same group and should belong to the same partition.
     *
     * @return The partition key on an event batch.
     */
    public String partitionKey() {
        return partitionKey;
    }
}
