// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import java.util.Objects;
import java.util.Optional;

/**
 * Options when receiving events from Event Hubs.
 */
public class ReceiverOptions {
    private final EventPosition position;
    private String name;
    private String consumerGroup;
    private String partitionId;
    private Long epoch;

    /**
     * Creates a new instance with the {@code position} to start receiving events from.
     *
     * @param partitionId Partition id for this receiver to get events from.
     * @param position Position to start receiving events from.
     */
    public ReceiverOptions(String partitionId, EventPosition position) {
        Objects.requireNonNull(partitionId);
        Objects.requireNonNull(position);

        this.partitionId = partitionId;
        this.position = position;
    }

    /**
     * Sets the name of the receiver.
     *
     * @param name The receiver name.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the name of the consumer group.
     *
     * @param consumerGroup The name of the consumer group.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions consumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Sets the epoch value on this receiver. When specified, this becomes an Epoch {@link EventHubReceiver}.
     * An Epoch receiver guarantees that only one {@link EventHubReceiver} can listen to each
     * "partition + consumer group" combination.
     *
     * @param epoch The Epoch value for this receiver.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions epoch(long epoch) {
        this.epoch = epoch;
        return this;
    }

    /**
     * Gets the name of the receiver.
     *
     * @return The name of the receiver.
     */
    public String name() {
        return name;
    }

    /**
     * Gets the name of the consumer group.
     *
     * @return The name of the consumer group.
     */
    public String consumerGroup() {
        return consumerGroup;
    }

    /**
     * Gets the partition name for this receiver.
     *
     * @return The partition name for this receiver.
     */
    public String partitionId() {
        return partitionId;
    }

    /**
     * Gets the position in the Event Hubs stream to start listening for events.
     *
     * @return The position in the Event Hubs stream to start listening for events.
     */
    public EventPosition position() {
        return position;
    }

    /**
     * Gets the epoch for this receiver. If {@link Optional#isPresent()} is {@code false}, then this is not an epoch
     * receiver. Otherwise, it is and there can only be one receiver per (partition + consumer group) combination.
     *
     * @return An optional epoch for this receiver.
     */
    public Optional<Long> epoch() {
        return Optional.ofNullable(epoch);
    }
}
