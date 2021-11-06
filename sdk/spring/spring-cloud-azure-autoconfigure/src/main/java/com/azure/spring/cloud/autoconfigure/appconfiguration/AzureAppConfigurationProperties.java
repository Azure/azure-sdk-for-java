// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationServiceVersion;
import com.azure.spring.cloud.autoconfigure.properties.AbstractAzureHttpCP;
import com.azure.spring.service.appconfiguration.AppConfigurationProperties;

/**
 * Properties for Azure App Configuration.
 */
public class AzureAppConfigurationProperties extends AbstractAzureHttpCP
    implements AppConfigurationProperties {

    public static final String PREFIX = "spring.cloud.azure.appconfiguration";

    private String endpoint;
    private String connectionString;
    private ConfigurationServiceVersion serviceVersion;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

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
