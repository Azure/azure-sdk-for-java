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
    public EventHubReceiver createReceiver(String partitionId, EventPosition position) {
        return new EventHubReceiver();
    }

    public EventHubReceiver createReceiver(ReceiverOptions options) {
        return new EventHubReceiver();
    }

    @Override
    public void close() {

    }
}
