// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

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
     * Creates a sender that transmits events to Event Hub. Event data is automatically routed to an available
     * partition.
     *
     * @return A new {@link EventSender}.
     */
    public EventSender createSender() {
        return new EventSender();
    }

    /**
     * Creates a sender that can push events to an Event Hub. If
     * {@link SenderOptions#partitionId() options.partitionId()} is specified, then the events are routed to that
     * specific partition. Otherwise, events are automatically routed to an available partition.
     *
     * @param options The set of options to apply when creating the sender.
     * @return A new {@link EventSender}.
     */
    public EventSender createSender(SenderOptions options) {
        return new EventSender(options);
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} starting from the moment it was created. The
     * consumer group used is the {@link ReceiverOptions#DEFAULT_CONSUMER_GROUP_NAME} consumer group.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @return An new {@link EventReceiver} that receives events from the partition at the given position.
     */
    public EventReceiver createReceiver(String partitionId) {
        return new EventReceiver();
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} at the given {@link EventPosition} with the
     * provided options.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @param options Additional options for the receiver.
     * @return An new {@link EventReceiver} that receives events from the partition at the given position.
     */
    public EventReceiver createReceiver(String partitionId, ReceiverOptions options) {
        return new EventReceiver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

    }
}
