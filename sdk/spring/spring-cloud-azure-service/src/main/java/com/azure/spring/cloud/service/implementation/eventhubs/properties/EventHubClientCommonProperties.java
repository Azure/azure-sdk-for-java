// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs.properties;

import com.azure.spring.cloud.core.aware.RetryOptionsAware;
import com.azure.spring.cloud.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.cloud.core.properties.AzureProperties;

/**
 * Azure Event Hubs common related properties.
 */
public interface EventHubClientCommonProperties extends AzureProperties, RetryOptionsAware, ConnectionStringAware {

    /**
     * Get the fully qualified namespace.
     * @return the fully qualified namespace.
     */
    String getFullyQualifiedNamespace();

    /**
     * Get the domain name.
     * @return the domain name.
     */
    String getDomainName();

    /**
     * Get the namespace.
     * @return the namespace.
     */
    String getNamespace();

    /**
     * Get the event hub name.
     * @return the event hub name.
     */
    String getEventHubName();

    /**
     * Get the custom endpoint address.
     * @return the custom endpoint address.
     */
    String getCustomEndpointAddress();

}
