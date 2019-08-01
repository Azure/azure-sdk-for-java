// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.implementation.annotation.Fluent;
import java.util.Objects;

/**
 * A model class to hold partition ownership information.
 */
@Fluent
public class PartitionOwnership {

    private String eventHubName;
    private String consumerGroupName;
    private String partitionId;
    private String instanceId;
    private long ownerLevel;
    private String offset; // optional
    private Long sequenceNumber; // optional
    private Long lastModifiedTime; // optional
    private String eTag; // optional

    /**
     * Gets the Event Hub name associated with this ownership record.
     *
     * @return The Event Hub name associated with this ownership record.
     */
    public String eventHubName() {
        return eventHubName;
    }

    /**
     * Sets the Event Hub name associated with this ownership record.
     *
     * @param eventHubName The Event Hub name associated with this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership eventHubName(String eventHubName) {
        this.eventHubName = Objects.requireNonNull(eventHubName, "eventHubName cannot be null");
        return this;
    }

    /**
     * Gets the consumer group name associated with this ownership record.
     *
     * @return The consumer group name associated with this ownership record.
     */
    public String consumerGroupName() {
        return consumerGroupName;
    }

    /**
     * Sets the consumer group name associated with this ownership record.
     *
     * @param consumerGroupName The consumer group name associated with this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership consumerGroupName(String consumerGroupName) {
        this.consumerGroupName = Objects.requireNonNull(consumerGroupName, "consumerGroupName cannot be null");
        return this;
    }

    /**
     * Gets the partition id associated with this ownership record.
     *
     * @return The partition id associated with this ownership record.
     */
    public String partitionId() {
        return partitionId;
    }

    /**
     * Sets the partition id associated with this ownership record.
     *
     * @param partitionId The partition id associated with this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership partitionId(String partitionId) {
        this.partitionId = Objects.requireNonNull(partitionId, "partitionId cannot be null");
        return this;
    }

    /**
     * Sets the unique event processor identifier as the owner of the partition id in this ownership record.
     *
     * @return The unique event processor identifier as the owner of the partition id in this ownership record.
     */
    public String instanceId() {
        return instanceId;
    }

    /**
     * Returns the unique event processor identifier that owns the partition id in this ownership record.
     *
     * @param instanceId The unique event processor identifier that owns the partition id in this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership instanceId(String instanceId) {
        this.instanceId = Objects.requireNonNull(instanceId, "instanceId cannot be null");
        return this;
    }

    /**
     * Gets the owner level (aka epoch number) for the event processor identified by {@link #instanceId() this instance}.
     *
     * @return The owner level (aka epoch number) for the event processor identified by {@link #instanceId() this
     * instance}
     */
    public long ownerLevel() {
        return ownerLevel;
    }

    /**
     * Sets the owner level (aka epoch number) for the event processor identified by {@link #instanceId() this instance}.
     *
     * @param ownerLevel The owner level (aka epoch number) for the event processor identified by {@link #instanceId()
     * this instance}.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership ownerLevel(long ownerLevel) {
        this.ownerLevel = ownerLevel;
        return this;
    }

    /**
     * Gets the offset that serves as checkpoint for the partition id in this ownership record.
     *
     * @return The offset that serves as checkpoint for the partition id in this ownership record.
     */
    public String offset() {
        return offset;
    }

    /**
     * Sets the offset that serves as checkpoint for the partition id in this ownership record.
     *
     * @param offset The offset that serves as checkpoint for the partition id in this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership offset(String offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Gets the sequence number that serves as checkpoint for the partition id in this ownership record.
     *
     * @return The sequence number that serves as checkpoint for the partition id in this ownership record.
     */
    public Long sequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequence number that serves as checkpoint for the partition id in this ownership record.
     *
     * @param sequenceNumber The sequence number that serves as checkpoint for the partition id in this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership sequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    /**
     * Gets the last modified time of this ownership record as epoch millis.
     *
     * @return The last modified time of this ownership record as epoch millis.
     */
    public Long lastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Sets the last modified time of this ownership record as epoch millis.
     *
     * @param lastModifiedTime The last modified time of this ownership record as epoch millis.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership lastModifiedTime(Long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }

    /**
     * TODO: add more details here
     * Gets the eTag.
     *
     * @return The eTag.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * TODO: add more details here
     * Sets the eTag.
     *
     * @param eTag The eTag.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership eTag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
