// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ClientConstants;

// Each time a build method is called, a new receiver or sender is created.
class EventHubClient implements AutoCloseable {
    // Creates a new sender.
    EventHubSender createSender() {
        return new EventHubSender(ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
    }

    // Creates a partition receiver that listens to the $DEFAULT consumer group starting at
    // the given position.
    // Throw exception at that point
    EventHubReceiver createReceiver(String partitionId, EventPosition position) {
        return new EventHubReceiver();
    }

    EventHubReceiver createReceiver(ReceiverOptions options) {
        return new EventHubReceiver();
    }

    @Override
    public void close() {

    }
}
