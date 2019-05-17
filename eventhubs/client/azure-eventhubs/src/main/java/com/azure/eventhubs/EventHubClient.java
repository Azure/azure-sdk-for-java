// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ClientConstants;

// Each time a build method is called, a new receiver or sender is created.
class EventHubClient implements AutoCloseable {

    EventHubClient() {
    }

    /**
     * Creates a builder that can configure options for the {@link EventHubClient} before creating an instance of it.
     *
     * @return A new {@link EventHubClientBuilder} to create an EventHubClient from.
     */
    public static EventHubClientBuilder builder() {
        return new EventHubClientBuilder();
    }

    // Creates a new sender.
    public EventHubSender createSender() {
        return new EventHubSender(ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
    }

    // Creates a partition receiver that listens to the $DEFAULT consumer group starting at
    // the given position.

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} at the given {@link EventPosition}. The
     * consumer group used is the "$DEFAULT" consumer group.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @param position Position within the partition's stream to start receiving events at.
     * @return An new {@link EventHubReceiver} that receives events from the partition at the given position.
     */
    public EventHubReceiver createReceiver(String partitionId, EventPosition position) {
        return new EventHubReceiver();
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} at the given {@link EventPosition} with the
     * provided options.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @param position Position within the partition's stream to start receiving events at.
     * @param options Additional options for the receiver.
     * @return An new {@link EventHubReceiver} that receives events from the partition at the given position.
     */
    public EventHubReceiver createReceiver(String partitionId, EventPosition position, ReceiverOptions options) {
        return new EventHubReceiver();
    }

    @Override
    public void close() {

    }
}
