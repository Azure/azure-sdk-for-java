// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.spring.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.core.properties.AzureProperties;

/**
 * Properties for Azure App Configuration.
 */
public interface AppConfigurationProperties extends AzureProperties, ConnectionStringAware {

    String getEndpoint();

    ConfigurationServiceVersion getServiceVersion();

}
