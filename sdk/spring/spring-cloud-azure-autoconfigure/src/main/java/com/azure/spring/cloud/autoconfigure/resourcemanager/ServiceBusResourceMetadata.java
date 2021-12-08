// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Azure Service Bus resource metadata
 */
@ConfigurationProperties(prefix = AzureServiceBusProperties.PREFIX + ".resource")
public class ServiceBusResourceMetadata extends AzureResourceMetadata {

    @Value("${spring.cloud.azure.servicebus.namespace:}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
