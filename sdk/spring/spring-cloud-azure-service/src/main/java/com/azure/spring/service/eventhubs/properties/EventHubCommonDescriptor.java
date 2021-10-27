// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import com.azure.spring.core.properties.AzureProperties;

/**
 * Azure Event Hub common related properties.
 */
public interface EventHubCommonDescriptor extends AzureProperties {

    String getFQDN();

    String getDomainName();

    String getNamespace();

    String getEventHubName();

    String getConnectionString();

    String getCustomEndpointAddress();

}
