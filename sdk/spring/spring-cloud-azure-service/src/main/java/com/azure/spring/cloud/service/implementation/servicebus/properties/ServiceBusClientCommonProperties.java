// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.properties;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.provider.connectionstring.ConnectionStringProvider;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

/**
 *
 */
public interface ServiceBusClientCommonProperties extends AzureProperties, RetryOptionsProvider, ConnectionStringProvider {

    String getFullyQualifiedNamespace();

    String getDomainName();

    /**
     * Get the namespace, which is the prefix of the FQDN. A FQDN should be composed of &lt;NamespaceName&gt;.&lt;DomainName&gt;
     * @return the namespace.
     */
    String getNamespace();

    String getEntityName();

    ServiceBusEntityType getEntityType();

    /**
     * Get the custom endpoint address.
     * @return the custom endpoint address.
     */
    String getCustomEndpointAddress();

    /**
     * Whether the shared {@code ServiceBusClientBuilder} should inherit the configuration derived from this sub-client's
     * properties. When {@code null} (the default) it is treated as {@code true}, preserving the existing behavior. Set to
     * {@code false} to keep configuration already present on the shared builder, for example {@code ClientOptions}
     * carrying {@code TracingOptions} set through an {@code AzureServiceClientBuilderCustomizer}.
     * @return whether the shared {@code ServiceBusClientBuilder} inherits this sub-client's property configuration.
     */
    Boolean getInheritConfiguration();
}
