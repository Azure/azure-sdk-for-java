// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor.models;

import com.azure.core.implementation.annotation.Fluent;

@Fluent
public class PartitionContext {
    private String partitionId;
    private String eventHubName;
    private String consumerGroupName;
    public String partitionId() {
        return partitionId;
    }

    public PartitionContext partitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    public String eventHubName() {
        return eventHubName;
    }

    public PartitionContext eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public String consumerGroupName() {
        return consumerGroupName;
    }

    public PartitionContext consumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
        return this;
    }
}
