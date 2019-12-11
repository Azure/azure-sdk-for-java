// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import static com.azure.core.util.FluxUtil.monoError;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import java.util.Objects;
import java.util.function.Consumer;
import reactor.core.publisher.Mono;

/**
 * A class that contains {@link EventData} and the partition information the event belongs to. This is given to the
 * {@link EventProcessorClientBuilder#processEvent(Consumer) processEvent} handler each time an event is received from
 * the Event Hub. This class also includes methods to update checkpoint in {@link CheckpointStore} and retrieve the last
 * enqueued event information.
 */
public class EventContext {

    private final ClientLogger logger = new ClientLogger(EventContext.class);
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
    public EventContext(PartitionContext partitionContext, EventData eventData,
        CheckpointStore checkpointStore, LastEnqueuedEventProperties lastEnqueuedEventProperties) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null.");
        this.eventData = Objects.requireNonNull(eventData, "'eventData' cannot be null.");
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
     * Returns the event data received from Event Hub.
     *
     * @return The event data received from Event Hub.
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
     * {@link EventProcessorClientBuilder#trackLastEnqueuedEventProperties(boolean)}is set to {@code false}, this
     * method will return {@code null}.
     */
    public LastEnqueuedEventProperties getLastEnqueuedEventProperties() {
        return lastEnqueuedEventProperties;
    }

    /**
     * Updates the checkpoint asynchronously for this partition using the event data. This will serve as the last known
     * successfully processed event in this partition if the update is successful.
     *
     * @param eventData The event data to use for updating the checkpoint.
     * @return a representation of deferred execution of this call.
     */
    public Mono<Void> updateCheckpointAsync(EventData eventData) {
        if (eventData == null) {
            return monoError(logger, new NullPointerException("'eventData' cannot be null"));
        }
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace(partitionContext.getFullyQualifiedNamespace())
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
        this.updateCheckpointAsync(eventData).block();
    }
}
