// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.spring.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.core.properties.AzureProperties;

/**
 * Properties for Azure App Configuration Client {@link com.azure.data.appconfiguration.ConfigurationClient}.
 */
public interface ConfigurationClientProperties extends AzureProperties, ConnectionStringAware {

    String getEndpoint();

    ConfigurationServiceVersion getServiceVersion();

}
