// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.eventhubs.EventData;

/**
 * The set of options that can be specified when sending a set of events to influence the way in which events are sent
 * to the Event Hubs service.
 */
@Fluent
public class SendOptions {
    private String partitionKey;
    private String partitionId;

    /**
     * Sets a hashing key to be provided for the batch of events, which instructs the Event Hubs service to map this key
     * to a specific partition.
     *
     * <p>The selection of a partition is stable for a given partition hashing key. Should any other batches of events
     * be sent using the same exact partition hashing key, the Event Hubs service will route them all to the same
     * partition.</p>
     *
     * <p>This should be specified only when there is a need to group events by partition, but there is flexibility into
     * which partition they are routed. If ensuring that a batch of events is sent only to a specific partition, it is
     * recommended that the {@link #setPartitionId(String) identifier of the position be specified directly} when
     * sending the batch.</p>
     *
     * @param partitionKey The partition hashing key to associate with the event or batch of events.
     *
     * @return The updated {@link SendOptions} object.
     */
    public SendOptions setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    /**
     * Gets the hashing key on an event batch. If specified, tells the Event Hubs service that these events
     * belong to the same group and should belong to the same partition.
     *
     * @return The partition hashing key to associate with the event or batch of events.
     */
    public String getPartitionKey() {
        return partitionKey;
    }

    /**
     * Gets the identifier of the Event Hub partition that the {@link EventData events} will be sent to. If the
     * identifier is not specified, the Event Hubs service will be responsible for routing events that are sent to an
     * available partition.
     *
     * @return The identifier of the Event Hub partition that the {@link EventData events} will be set to. {@code null}
     *     or an empty string if Event Hubs service is responsible for routing events.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Sets the identifier of the Event Hub partition that the {@link EventData events} will be sent to. If the
     * identifier is not specified, the Event Hubs service will be responsible for routing events that are sent to an
     * available partition.
     *
     * @param partitionId The identifier of the Event Hub partition that the {@link EventData events} will be set
     *     to. {@code null} or an empty string if Event Hubs service is responsible for routing events.
     *
     * @return The updated {@link SendOptions} object.
     */
    public SendOptions setPartitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }
}
