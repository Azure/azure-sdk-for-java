// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Fluent;
import java.util.Objects;

/**
 * A model class to hold partition ownership information.
 */
@Fluent
public class PartitionOwnership {

    private String eventHubName;
    private String consumerGroupName;
    private String partitionId;
    private String ownerId;
    private long ownerLevel;
    private Long offset;
    private Long sequenceNumber;
    private Long lastModifiedTime;
    private String eTag;

    /**
     * Gets the Event Hub name associated with this ownership record.
     *
     * @return The Event Hub name associated with this ownership record.
     */
    public String getEventHubName() {
        return eventHubName;
    }

    /**
     * Sets the Event Hub name associated with this ownership record.
     *
     * @param eventHubName The Event Hub name associated with this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setEventHubName(String eventHubName) {
        this.eventHubName = Objects.requireNonNull(eventHubName, "eventHubName cannot be null.");
        return this;
    }

    /**
     * Gets the consumer group name associated with this ownership record.
     *
     * @return The consumer group name associated with this ownership record.
     */
    public String getConsumerGroupName() {
        return consumerGroupName;
    }

    /**
     * Sets the consumer group name associated with this ownership record.
     *
     * @param consumerGroupName The consumer group name associated with this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setConsumerGroupName(String consumerGroupName) {
        this.consumerGroupName = Objects.requireNonNull(consumerGroupName, "consumerGroupName cannot be null.");
        return this;
    }

    /**
     * Gets the partition id associated with this ownership record.
     *
     * @return The partition id associated with this ownership record.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Sets the partition id associated with this ownership record.
     *
     * @param partitionId The partition id associated with this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setPartitionId(String partitionId) {
        this.partitionId = Objects.requireNonNull(partitionId, "partitionId cannot be null.");
        return this;
    }

    /**
     * Sets the unique event processor identifier as the owner of the partition id in this ownership record.
     *
     * @return The unique event processor identifier as the owner of the partition id in this ownership record.
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Returns the unique event processor identifier that owns the partition id in this ownership record.
     *
     * @param ownerId The unique event processor identifier that owns the partition id in this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setOwnerId(String ownerId) {
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId cannot be null.");
        return this;
    }

    /**
     * Gets the owner level (aka epoch number) for the event processor identified by
     * {@link #getOwnerId() this instance}.
     *
     * @return The owner level (aka epoch number) for the event processor identified by {@link #getOwnerId() this
     *     instance}.
     */
    public long getOwnerLevel() {
        return ownerLevel;
    }

    /**
     * Sets the owner level (aka epoch number) for the event processor identified by
     * {@link #getOwnerId() this instance}.
     *
     * @param ownerLevel The owner level (aka epoch number) for the event processor identified by {@link #getOwnerId()
     *     this instance}.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setOwnerLevel(long ownerLevel) {
        this.ownerLevel = ownerLevel;
        return this;
    }

    /**
     * Gets the offset that serves as checkpoint for the partition id in this ownership record.
     *
     * @return The offset that serves as checkpoint for the partition id in this ownership record.
     */
    public Long getOffset() {
        return offset;
    }

    /**
     * Sets the offset that serves as checkpoint for the partition id in this ownership record.
     *
     * @param offset The offset that serves as checkpoint for the partition id in this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setOffset(Long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Gets the sequence number that serves as checkpoint for the partition id in this ownership record.
     *
     * @return The sequence number that serves as checkpoint for the partition id in this ownership record.
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequence number that serves as checkpoint for the partition id in this ownership record.
     *
     * @param sequenceNumber The sequence number that serves as checkpoint for the partition id in this ownership
     *     record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    /**
     * Gets the last modified time of this ownership record as epoch millis.
     *
     * @return The last modified time of this ownership record as epoch millis.
     */
    public Long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Sets the last modified time of this ownership record as epoch millis.
     *
     * @param lastModifiedTime The last modified time of this ownership record as epoch millis.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setLastModifiedTime(Long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }

    /**
     * Gets the ETag that was generated by the last known successful update to the partition ownership record. An ETag
     * is a unique identifier * that is generated when a data record is successfully created/updated. The ETag is used
     * to achieve optimistic concurrency in a distributed event processor setup. When multiple instances of event
     * processor try to update the same partition ownership record, ETag is used to verify that the last values read by
     * the instance requesting the update is still the latest ETag for this record. If the ETag in the store does not
     * match the ETag in the update request, then the update is expected to fail as there was an update since the last
     * time an event processor read this record.
     *
     * @return The eTag for this ownership record.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets the ETag with the last known successful update to partition ownership record. An ETag is a unique identifier
     * that is generated when a data record is successfully created/updated. This ETag is used to achieve optimistic
     * concurrency in a distributed event processor setup. When multiple instances of event processor try to update the
     * same partition ownership record, ETag is used to verify that the last values read by the instance requesting the
     * update is still the latest ETag for this record. If the ETag in the store does not match the ETag in the update
     * request, then the update is expected to fail as there was an update since the last time an event processor read
     * this record.
     *
     * @param eTag The eTag for this ownership record.
     * @return The updated {@link PartitionOwnership} instance.
     */
    public PartitionOwnership setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
