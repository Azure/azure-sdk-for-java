// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs.properties;

/**
 * Azure Event Hubs related properties.
 */
public interface EventHubsNamespaceProperties extends EventHubClientCommonProperties {

    /**
     * Get the shared connection switch.
     * @return the shared connection switch.
     */
    Boolean getSharedConnection();

}
