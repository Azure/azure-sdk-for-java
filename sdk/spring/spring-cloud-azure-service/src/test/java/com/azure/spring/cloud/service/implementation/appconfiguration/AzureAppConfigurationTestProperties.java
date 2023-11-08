// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;

/**
 * Properties for Azure Storage File Share service.
 */
class AzureAppConfigurationTestProperties extends AzureHttpSdkProperties implements ConfigurationClientProperties {

    private String connectionString;
    private String endpoint;
    private ConfigurationServiceVersion serviceVersion;

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public ConfigurationServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(ConfigurationServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
