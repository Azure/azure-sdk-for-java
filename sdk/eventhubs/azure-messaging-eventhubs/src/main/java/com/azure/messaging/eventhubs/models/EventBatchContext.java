// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import reactor.core.publisher.Mono;

/**
 * A class that contains a batch of {@link EventData} and the partition information the event batch belongs to. This is
 * given to the {@link EventProcessorClientBuilder#processEventBatch(Consumer, int) processEventBatch} handler each
 * time an event batch is received from the Event Hub. This class also includes methods to update checkpoint in
 * {@link CheckpointStore} and retrieve the last enqueued event information.
 *
 * @see EventProcessorClientBuilder#processEventBatch(Consumer, int)
 * @see EventProcessorClientBuilder#processEventBatch(Consumer, int, Duration)
 */
public class EventBatchContext {

    private final PartitionContext partitionContext;
    private final List<EventData> events;
    private final CheckpointStore checkpointStore;
    private final LastEnqueuedEventProperties lastEnqueuedEventProperties;

    /**
     * Creates an instance of {@link EventContext}.
     *
     * @param partitionContext The partition information associated with the received event.
     * @param events The list of events received from Event Hub.
     * @param checkpointStore The checkpoint store that is used for updating checkpoints.
     * @param lastEnqueuedEventProperties The properties of the last enqueued event in this partition. If {@link
     * EventProcessorClientBuilder#trackLastEnqueuedEventProperties(boolean)} is set to {@code false}, this will be
     * {@code null}.
     * @throws NullPointerException If {@code partitionContext}, {@code eventData} or {@code checkpointStore} is null.
     */
    public EventBatchContext(PartitionContext partitionContext, List<EventData> events,
        CheckpointStore checkpointStore, LastEnqueuedEventProperties lastEnqueuedEventProperties) {
        this.checkpointStore = Objects.requireNonNull(checkpointStore, "'checkpointStore' cannot be null.");
        this.events = Objects.requireNonNull(events, "'events' cannot be null.");
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null.");

        this.lastEnqueuedEventProperties = lastEnqueuedEventProperties;
    }

    /**
     * Returns the partition information associated with the received event.
     *
     * @return The partition information of the received event.
     */
    public PartitionContext getPartitionContext() {
        return partitionContext;
    }

    /**
     * Returns a list of event data received from Event Hub.
     *
     * @return The list of event data received from Event Hub.
     */
    public List<EventData> getEvents() {
        return events;
    }

    /**
     * Returns the properties of the last enqueued event in this partition. If {@link
     * EventProcessorClientBuilder#trackLastEnqueuedEventProperties(boolean)} is set to {@code false} or if
     * {@link #getEvents()} is empty, this method will return {@code null}.
     *
     * @return The properties of the last enqueued event in this partition. If
     * {@link EventProcessorClientBuilder#trackLastEnqueuedEventProperties(boolean)} is
     * set to {@code false} or if {@link #getEvents()} is empty, this method will return {@code null}.
     */
    public LastEnqueuedEventProperties getLastEnqueuedEventProperties() {
        return lastEnqueuedEventProperties;
    }

    /**
     * Updates the checkpoint asynchronously for this partition using the last event in the list provided by
     * {@link #getEvents()}. This will serve as the last known successfully processed event in this partition
     * if the update is successful. If {@link #getEvents()} returns an empty, no update to checkpoint will be
     * done.
     *
     * @return Gets a {@link Mono} that completes when the checkpoint is updated.
     * @throws AmqpException if an error occurs when updating the checkpoint.
     */
    public Mono<Void> updateCheckpointAsync() {
        if (this.events.isEmpty()) {
            return Mono.empty();
        }

        // update checkpoint of the last event in the batch
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace(partitionContext.getFullyQualifiedNamespace())
            .setEventHubName(partitionContext.getEventHubName())
            .setConsumerGroup(partitionContext.getConsumerGroup())
            .setPartitionId(partitionContext.getPartitionId())
            .setSequenceNumber(events.get(events.size() - 1).getSequenceNumber())
            .setOffset(events.get(events.size() - 1).getOffset());
        return this.checkpointStore.updateCheckpoint(checkpoint);
    }

    /**
     * Updates the checkpoint synchronously for this partition using the last event in the list provided by
     * {@link #getEvents()}. This will serve as the last known successfully processed event in this partition
     * if the update is successful. If {@link #getEvents()} returns an empty, no update to checkpoint will be
     * done.
     *
     * @throws AmqpException if an error occurs while updating the checkpoint.
     */
    public void updateCheckpoint() {
        this.updateCheckpointAsync().block();
    }
}
