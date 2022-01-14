// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.eventhubs.properties;

import com.azure.spring.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.core.properties.AzureProperties;

/**
 * Azure Event Hubs common related properties.
 */
public interface EventHubClientCommonProperties extends AzureProperties, ConnectionStringAware {

    String getFullyQualifiedNamespace();

    String getDomainName();

    String getNamespace();

    String getEventHubName();

    String getCustomEndpointAddress();

}
