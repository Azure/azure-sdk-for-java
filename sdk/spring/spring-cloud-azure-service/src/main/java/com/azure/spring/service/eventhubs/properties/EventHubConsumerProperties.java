// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

/**
 * Azure Event Hubs Consumer related properties.
 */
public interface EventHubConsumerProperties extends EventHubClientCommonProperties {

    String getConsumerGroup();

    Integer getPrefetchCount();

}
