// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor.models;

import com.azure.core.implementation.annotation.Fluent;

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

    public String eventHubName() {
        return eventHubName;
    }

    public PartitionOwnership eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public String consumerGroupName() {
        return consumerGroupName;
    }

    public PartitionOwnership consumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
        return this;
    }

    public String partitionId() {
        return partitionId;
    }

    public PartitionOwnership partitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    public String instanceId() {
        return instanceId;
    }

    public PartitionOwnership instanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public long ownerLevel() {
        return ownerLevel;
    }

    public PartitionOwnership ownerLevel(long ownerLevel) {
        this.ownerLevel = ownerLevel;
        return this;
    }

    public String offset() {
        return offset;
    }

    public PartitionOwnership offset(String offset) {
        this.offset = offset;
        return this;
    }

    public Long sequenceNumber() {
        return sequenceNumber;
    }

    public PartitionOwnership sequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    public Long lastModifiedTime() {
        return lastModifiedTime;
    }

    public PartitionOwnership lastModifiedTime(Long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }

    public String eTag() {
        return eTag;
    }

    public PartitionOwnership eTag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
