// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.aware.authentication.ConnectionStringAware;

/**
 * Azure Event Hubs common related properties.
 */
public interface EventHubsCommonDescriptor extends AzureProperties, ConnectionStringAware {

    String getFQDN();

    String getDomainName();

    String getNamespace();

    String getEventHubName();

    String getCustomEndpointAddress();

}
