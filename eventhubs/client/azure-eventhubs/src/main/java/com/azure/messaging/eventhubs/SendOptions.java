// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

/**
 * The set of options that can be specified when sending a set of events to influence the way in which events are sent
 * to the Event Hubs service.
 *
 * @see EventHubProducer
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
     * @return The updated {@link SendOptions} object.
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
    public SendOptions partitionKey(String partitionKey) {
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
}
