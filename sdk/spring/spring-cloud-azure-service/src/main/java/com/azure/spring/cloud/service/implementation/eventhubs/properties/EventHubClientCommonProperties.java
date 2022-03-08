// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs.properties;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.provider.connectionstring.ConnectionStringProvider;

/**
 * Azure Event Hubs common related properties.
 */
public interface EventHubClientCommonProperties extends AzureProperties, RetryOptionsProvider, ConnectionStringProvider {

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
     * Get the namespace, which is the prefix of the FQDN. A FQDN should be composed of &lt;NamespaceName&gt;.&lt;DomainName&gt;
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
