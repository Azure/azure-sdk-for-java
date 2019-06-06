// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.eventhubs.EventHubProperties;
import com.azure.eventhubs.PartitionProperties;
import reactor.core.publisher.Mono;

import java.io.Closeable;

public interface EventHubManagementNode extends Closeable {
    Mono<EventHubProperties> getEventHubProperties();

    Mono<PartitionProperties> getPartitionProperties(String partitionId);
}
