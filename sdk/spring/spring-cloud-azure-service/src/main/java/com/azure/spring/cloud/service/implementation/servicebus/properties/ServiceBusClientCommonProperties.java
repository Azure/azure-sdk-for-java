// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.properties;

import com.azure.spring.cloud.core.aware.RetryOptionsAware;
import com.azure.spring.cloud.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

/**
 *
 */
public interface ServiceBusClientCommonProperties extends AzureProperties, RetryOptionsAware, ConnectionStringAware {

    String getFullyQualifiedNamespace();

    String getDomainName();

    /**
     * Get the namespace, which is the prefix of the FQDN. A FQDN should be composed of &lt;NamespaceName&gt;.&lt;DomainName&gt;
     * @return the namespace.
     */
    String getNamespace();

    String getConnectionString();

    String getEntityName();

    ServiceBusEntityType getEntityType();

}
