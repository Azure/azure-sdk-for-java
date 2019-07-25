// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor.models;

import com.azure.core.implementation.annotation.Fluent;

@Fluent
public class Checkpoint {
    private String eventHubName;
    private String consumerGroupName;
    private String instanceId;
    private String partitionId;
    private String offset;
    private long sequenceNumber;

    public String eventHubName() {
        return eventHubName;
    }

    public Checkpoint eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public String consumerGroupName() {
        return consumerGroupName;
    }

    public Checkpoint consumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
        return this;
    }

    public String instanceId() {
        return instanceId;
    }

    public Checkpoint instanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String partitionId() {
        return partitionId;
    }

    public Checkpoint partitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    public Checkpoint sequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    public String offsetNumber() {
        return offset;
    }

    public Checkpoint offset(String offset) {
        this.offset = offset;
        return this;
    }
}
