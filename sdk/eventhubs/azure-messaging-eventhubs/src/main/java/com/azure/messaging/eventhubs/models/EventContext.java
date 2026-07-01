// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A class that contains {@link EventData} and the partition information the event belongs to. This is given to the
 * {@link EventProcessorClientBuilder#processEvent(Consumer) processEvent} and
 * {@link EventProcessorClientBuilder#processEvent(Consumer, Duration)} handlers each time an event is received from
 * the Event Hub or when the {@code maxWaitTime} duration has elapsed. This class also includes methods to update
 * checkpoint in {@link CheckpointStore} and retrieve the last enqueued event information.
 */
public class EventContext {

    private final PartitionContext partitionContext;
    private final EventData eventData;
    private final CheckpointStore checkpointStore;
    private final LastEnqueuedEventProperties lastEnqueuedEventProperties;

    /**
     * Creates an instance of {@link EventContext}.
     *
     * @param partitionContext The partition information associated with the received event.
     * @param eventData The event received from Event Hub.
     * @param checkpointStore The checkpoint store that is used for updating checkpoints.
     * @param lastEnqueuedEventProperties The properties of the last enqueued event in this partition. If {@link
     * EventProcessorClientBuilder#trackLastEnqueuedEventProperties(boolean)} is set to {@code false}, this will be
     * {@code null}.
     * @throws NullPointerException If {@code partitionContext}, {@code eventData} or {@code checkpointStore} is null.
     */
    public EventContext(PartitionContext partitionContext, EventData eventData, CheckpointStore checkpointStore,
        LastEnqueuedEventProperties lastEnqueuedEventProperties) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null.");
        this.eventData = eventData;
        this.checkpointStore = Objects.requireNonNull(checkpointStore, "'checkpointStore' cannot be null.");
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
     * Returns the event data received from Event Hub.  Can be {@code null} if
     * {@link EventProcessorClientBuilder#processEvent(Consumer, Duration)} was used to construct the processor.  This
     * means that no event was received during the specified duration.
     *
     * @return The event data received from Event Hub or {@code null}.
     */
    public EventData getEventData() {
        return eventData;
    }

    /**
     * Returns the properties of the last enqueued event in this partition. If {@link
     * EventProcessorClientBuilder#trackLastEnqueuedEventProperties(boolean)} is set to {@code false}, this method will
     * return {@code null}.
     *
     * @return The properties of the last enqueued event in this partition. If
     * {@link EventProcessorClientBuilder#trackLastEnqueuedEventProperties(boolean)} is
     * set to {@code false}, this method will return {@code null}.
     */
    public LastEnqueuedEventProperties getLastEnqueuedEventProperties() {
        return lastEnqueuedEventProperties;
    }

    /**
     * Updates the checkpoint asynchronously for this partition using the event data in this {@link EventContext}. This
     * will serve as the last known successfully processed event in this partition if the update is successful.
     *
     * @return A representation of deferred execution of this call.
     */
    public Mono<Void> updateCheckpointAsync() {
        if (eventData == null) {
            return Mono.empty();
        }
        Checkpoint checkpoint
            = new Checkpoint().setFullyQualifiedNamespace(partitionContext.getFullyQualifiedNamespace())
                .setEventHubName(partitionContext.getEventHubName())
                .setConsumerGroup(partitionContext.getConsumerGroup())
                .setPartitionId(partitionContext.getPartitionId())
                .setSequenceNumber(eventData.getSequenceNumber())
                .setOffset(eventData.getOffset());
        return this.checkpointStore.updateCheckpoint(checkpoint);
    }

    /**
     * Updates the checkpoint synchronously for this partition using the event data. This will serve as the last known
     * successfully processed event in this partition if the update is successful.
     */
    public void updateCheckpoint() {
        this.updateCheckpointAsync().block();
    }
}
