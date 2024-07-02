// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties;


import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.springframework.boot.context.properties.NestedConfigurationProperty;



public class AadProfileProperties {
    /**
     * Azure Tenant ID.
     */
    private String tenantId; // tenantId can not set to "common" here, otherwise we can not know whether it's set by customer or it is the default value.
    /**
     * Name of the Azure cloud to connect to. Supported types are: AZURE, AZURE_CHINA, AZURE_US_GOVERNMENT, OTHER. The default value is `AZURE`.
     */
    private AzureProfileOptionsProvider.CloudType cloudType;
    /**
     * Properties to Azure Active Directory endpoints.
     */
    @NestedConfigurationProperty
    private final AadProfileEnvironmentProperties environment = new AadProfileEnvironmentProperties();

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public AzureProfileOptionsProvider.CloudType getCloudType() {
        return cloudType;
    }

    public void setCloudType(AzureProfileOptionsProvider.CloudType cloudType) {
        this.cloudType = cloudType;
        environment.updatePropertiesByCloudType(cloudType);
    }

    public AadProfileEnvironmentProperties getEnvironment() {
        return environment;
    }

}
