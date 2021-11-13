// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.properties;

/**
 * Batchable producer properties interface for configuration of batch sending.
 */
public interface EventHubBatchableProducerDescriptor {

    Integer getMaxBatchInBytes();

}
