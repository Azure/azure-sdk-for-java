// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.properties;

import com.azure.spring.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.core.properties.AzureProperties;

/**
 *
 */
public interface ServiceBusCommonDescriptor extends AzureProperties, ConnectionStringAware {

    String getFQDN();

    String getDomainName();

    String getNamespace();

    String getConnectionString();

    String getEntityName();

    ServiceBusEntityType getEntityType();

}
