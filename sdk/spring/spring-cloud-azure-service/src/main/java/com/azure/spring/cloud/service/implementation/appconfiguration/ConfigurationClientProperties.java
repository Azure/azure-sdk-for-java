// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.provider.connectionstring.ConnectionStringProvider;

/**
 * Properties for Azure App Configuration Client {@link com.azure.data.appconfiguration.ConfigurationClient}.
 */
public interface ConfigurationClientProperties extends AzureProperties, RetryOptionsProvider, ConnectionStringProvider {

    /**
     * Get the service endpoint.
     * @return the service endpoint.
     */
    String getEndpoint();

    /**
     * Get the service version.
     * @return the service version.
     */
    ConfigurationServiceVersion getServiceVersion();

}
