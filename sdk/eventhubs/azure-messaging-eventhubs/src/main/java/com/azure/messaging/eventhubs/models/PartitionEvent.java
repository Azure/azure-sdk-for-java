// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A container for {@link EventData} along with the partition information for this event data.
 */
@Immutable
public class PartitionEvent {

    private final PartitionContext partitionContext;
    private final EventData eventData;
    private final LastEnqueuedEventProperties lastEnqueuedEventProperties;
    private final ObjectSerializer serializer;
    private final ClientLogger logger = new ClientLogger(PartitionEvent.class);

    /**
     * Creates an instance of PartitionEvent.
     *
     * @param partitionContext The partition information associated with the event data.
     * @param eventData The event data received from the Event Hub.
     * @param lastEnqueuedEventProperties The properties of the last enqueued event in the partition.
     * @throws NullPointerException if {@code partitionContext} or {@code eventData} is {@code null}.
     */
    public PartitionEvent(final PartitionContext partitionContext, final EventData eventData,
                          LastEnqueuedEventProperties lastEnqueuedEventProperties) {
        this(partitionContext, eventData, lastEnqueuedEventProperties, null);
    }

    /**
     * Creates an instance of PartitionEvent.
     *
     * @param partitionContext The partition information associated with the event data.
     * @param eventData The event data received from the Event Hub.
     * @param lastEnqueuedEventProperties The properties of the last enqueued event in the partition.
     * @param serializer ObjectSerializer implementation for deserializing event data payload.  May be null.
     * @throws NullPointerException if {@code partitionContext} or {@code eventData} is {@code null}.
     */
    public PartitionEvent(final PartitionContext partitionContext, final EventData eventData,
        LastEnqueuedEventProperties lastEnqueuedEventProperties, ObjectSerializer serializer) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null");
        this.eventData = Objects.requireNonNull(eventData, "'eventData' cannot be null");
        this.lastEnqueuedEventProperties = lastEnqueuedEventProperties;
        this.serializer = serializer;
    }

    /**
     * Returns the partition information associated with the event data.
     *
     * @return The partition information associated with the event data.
     */
    public PartitionContext getPartitionContext() {
        return partitionContext;
    }

    /**
     * Gets the event received from the partition.
     *
     * @return Event received from the partition.
     */
    public EventData getData() {
        return eventData;
    }

    /**
     * Gets the properties of the last enqueued event in this partition.
     *
     * @return The properties of the last enqueued event in this partition.
     */
    public LastEnqueuedEventProperties getLastEnqueuedEventProperties() {
        return lastEnqueuedEventProperties;
    }

    /**
     * Deserializes event payload into object.
     *
     * @param objectType Class object of type T
     * @param <T> object type for deserialization
     * @return deserialized object as type T
     */
    public <T> Mono<T> getDeserializedObject(Class<T> objectType) {
        if (this.serializer == null) {
            return monoError(logger,
                new NullPointerException("No serializer set for deserializing EventData payload."));
        }
        if (objectType == null) {
            return monoError(logger, new IllegalArgumentException("objectType cannot be null."));
        }

        return serializer.deserialize(new ByteArrayInputStream(eventData.getBody()), objectType);
    }
}
