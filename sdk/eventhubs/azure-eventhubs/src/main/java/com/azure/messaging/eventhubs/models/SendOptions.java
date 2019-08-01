// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.implementation.annotation.Fluent;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducer;
import reactor.core.publisher.Flux;

/**
 * The set of options that can be specified when sending a set of events to influence the way in which events are sent
 * to the Event Hubs service.
 *
 * @see EventHubProducer#send(EventData, SendOptions)
 * @see EventHubProducer#send(Iterable, SendOptions)
 * @see EventHubProducer#send(Flux, SendOptions)
 */
@Fluent
public class SendOptions implements Cloneable {
    private String partitionKey;

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

    /**
     * Creates a shallow clone of this instance.
     *
     * @return A shallow clone of this object.
     */
    @Override
    public SendOptions clone() {
        SendOptions clone;
        try {
            clone = (SendOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new SendOptions();
        }

        return clone.partitionKey(partitionKey);
    }
}
