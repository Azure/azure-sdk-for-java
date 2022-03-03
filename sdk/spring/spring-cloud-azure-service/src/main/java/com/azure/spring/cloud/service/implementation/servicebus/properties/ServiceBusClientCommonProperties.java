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

    String getNamespace();

    String getConnectionString();

    String getEntityName();

    ServiceBusEntityType getEntityType();

}
