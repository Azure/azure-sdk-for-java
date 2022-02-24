// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.resourcemanager;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.resourcemanager.AzureResourceMetadataConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Azure Event Hubs resource metadata.
 */
@ConfigurationProperties(prefix = AzureEventHubsProperties.PREFIX + ".resource")
public class EventHubsResourceMetadata extends AzureResourceMetadataConfigurationProperties {

    /**
     * Namespace of the event hub.
     */
    @Value("${spring.cloud.azure.eventhubs.namespace:}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
