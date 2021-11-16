// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

/**
 * Azure Event Hub Consumer related properties.
 */
public interface EventHubsConsumerDescriptor extends EventHubsCommonDescriptor {

    String getConsumerGroup();

    Integer getPrefetchCount();

}
