// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor.models;

import com.azure.core.implementation.annotation.Fluent;

/**
 * A model class to hold checkpoint data.
 */
@Fluent
public class Checkpoint {

    private String eventHubName;
    private String consumerGroupName;
    private String instanceId;
    private String partitionId;
    private String offset;
    private long sequenceNumber;
    private String eTag;

    /**
     * Gets the event hub name.
     *
     * @return The event hub name.
     */
    public String eventHubName() {
        return eventHubName;
    }

    /**
     * Sets the event hub name.
     *
     * @param eventHubName The event hub name.
     * @return The updated {@link Checkpoint} instance.
     */
    public Checkpoint eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    /**
     * Gets the consumer group name.
     *
     * @return The consumer group name.
     */
    public String consumerGroupName() {
        return consumerGroupName;
    }

    /**
     * Sets the consumer group name.
     *
     * @param consumerGroupName The consumer group name.
     * @return The updated {@link Checkpoint} instance.
     */
    public Checkpoint consumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
        return this;
    }

    /**
     * Gets the partition id.
     *
     * @return The partition id.
     */
    public String partitionId() {
        return partitionId;
    }

    /**
     * Sets the partition id.
     *
     * @param partitionId The partition id.
     * @return The updated {@link Checkpoint} instance.
     */
    public Checkpoint partitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    /**
     * Sets the unique instance identifier.
     *
     * @return The instance identifier.
     */
    public String instanceId() {
        return instanceId;
    }

    /**
     * Returns the instance identifier.
     *
     * @param instanceId The instance identifier.
     * @return The updated {@link Checkpoint} instance.
     */
    public Checkpoint instanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    /**
     * Gets the offset.
     *
     * @return The offset.
     */
    public String offset() {
        return offset;
    }

    /**
     * Sets the offset.
     *
     * @param offset The offset.
     * @return The updated {@link Checkpoint} instance.
     */
    public Checkpoint offset(String offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Gets the sequence number.
     *
     * @return The sequence number.
     */
    public Long sequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequence number.
     *
     * @param sequenceNumber The sequence number.
     * @return The updated {@link Checkpoint} instance.
     */
    public Checkpoint sequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    /**
     * Gets the eTag.
     *
     * @return The eTag.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * Sets the eTag.
     *
     * @param eTag The eTag.
     * @return The updated {@link Checkpoint} instance.
     */
    public Checkpoint eTag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
