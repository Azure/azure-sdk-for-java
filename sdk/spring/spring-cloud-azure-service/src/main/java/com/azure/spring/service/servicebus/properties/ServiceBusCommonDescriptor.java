// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.properties;

import com.azure.spring.core.properties.AzureProperties;

/**
 *
 */
public interface ServiceBusCommonDescriptor extends AzureProperties {

    String getFQDN();

    String getDomainName();

    String getNamespace();

    String getConnectionString();

}
