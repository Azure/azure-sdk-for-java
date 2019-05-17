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

    /**
     * Creates a sender that can push events to Event Hub.
     *
     * @return A new {@link EventSender}.
     */
    public EventSender createSender() {
        return new EventSender(ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} at the given {@link EventPosition}. The
     * consumer group used is the {@link ReceiverOptions#DEFAULT_CONSUMER_GROUP_NAME} consumer group.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @param position Position within the partition's stream to start receiving events at.
     * @return An new {@link EventReceiver} that receives events from the partition at the given position.
     */
    public EventReceiver createReceiver(String partitionId, EventPosition position) {
        return new EventReceiver();
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} at the given {@link EventPosition} with the
     * provided options.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @param position Position within the partition's stream to start receiving events at.
     * @param options Additional options for the receiver.
     * @return An new {@link EventReceiver} that receives events from the partition at the given position.
     */
    public EventReceiver createReceiver(String partitionId, EventPosition position, ReceiverOptions options) {
        return new EventReceiver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

    }
}
