// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import com.azure.spring.core.properties.AzureProperties;

/**
 * Azure Event Hub related properties.
 */
public interface EventHubCommonProperties extends AzureProperties {

    String getFQDN();

    String getDomainName();

    String getNamespace();

    String getEventHubName();

    String getConnectionString();

    String getCustomEndpointAddress();

    Integer getPrefetchCount();

}
