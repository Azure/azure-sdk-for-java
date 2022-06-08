// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;


import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.springframework.boot.context.properties.NestedConfigurationProperty;



/**
 * Profile of Azure cloud environment.
 */
public class AadProfileProperties {
    /**
     * Azure Tenant ID.
     */
    private String tenantId; // tenantId can not set to "common" here, otherwise we can not know whether it's set by customer or it is the default value.
    /**
     * Name of the Azure cloud to connect to.
     * Supported types are: AZURE, AZURE_CHINA, AZURE_GERMANY, AZURE_US_GOVERNMENT, OTHER.
     */
    private AzureProfileOptionsProvider.CloudType cloudType;
    /**
     * Properties to Azure Active Directory endpoints.
     */
    @NestedConfigurationProperty
    private final AadProfileEnvironmentProperties environment = new AadProfileEnvironmentProperties();

    /**
     *
     * @return The tenant ID.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     *
     * @param tenantId The tenant ID.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     *
     * @return The cloud type.
     */
    public AzureProfileOptionsProvider.CloudType getCloudType() {
        return cloudType;
    }

    /**
     *
     * @param cloudType The cloud type.
     */
    public void setCloudType(AzureProfileOptionsProvider.CloudType cloudType) {
        this.cloudType = cloudType;
        environment.updatePropertiesByCloudType(cloudType);
    }

    /**
     *
     * @return The AADProfileEnvironmentProperties.
     */
    public AadProfileEnvironmentProperties getEnvironment() {
        return environment;
    }

}
