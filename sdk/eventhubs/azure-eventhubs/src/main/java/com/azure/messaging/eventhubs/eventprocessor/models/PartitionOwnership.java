// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor.models;

import com.azure.core.implementation.annotation.Fluent;

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
    private String offset; // can be null
    private Long sequenceNumber; // can be null
    private Long lastModifiedTime; // can be null
    private String eTag; // can be null

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
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership eventHubName(String eventHubName) {
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
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership consumerGroupName(String consumerGroupName) {
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
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership partitionId(String partitionId) {
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
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership instanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    /**
     * Gets the owner level.
     *
     * @return The owner level.
     */
    public long ownerLevel() {
        return ownerLevel;
    }

    /**
     * Sets the owner level.
     *
     * @param ownerLevel The owner level.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership ownerLevel(long ownerLevel) {
        this.ownerLevel = ownerLevel;
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
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership offset(String offset) {
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
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership sequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    /**
     * Gets the last modified time as epoch millis.
     *
     * @return The last modified time.
     */
    public Long lastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Sets the last modified time as epoch millis.
     *
     * @param lastModifiedTime The last modified time.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership lastModifiedTime(Long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
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
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership eTag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
