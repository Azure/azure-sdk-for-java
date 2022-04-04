// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientProperties;

/**
 * Properties for Azure App Configuration.
 */
public class AzureAppConfigurationProperties extends AbstractAzureHttpConfigurationProperties
    implements ConfigurationClientProperties {

    public static final String PREFIX = "spring.cloud.azure.appconfiguration";

    /**
     * Endpoint of the Azure App Configuration instance.
     */
    private String endpoint;
    /**
     * Connection string of the Azure App Configuration instance.
     */
    private String connectionString;
    /**
     * Version of the app configuration service to be used when making request.
     */
    private ConfigurationServiceVersion serviceVersion;

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public ConfigurationServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(ConfigurationServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
