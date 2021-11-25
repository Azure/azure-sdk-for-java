// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.azure.spring.core.aware.AzureProfileAware;

/**
 * Profile of Azure cloud environment.
 */
public class AADProfileProperties {
    /**
     * Azure Tenant ID.
     */
    private String tenantId;
    /**
     * Name of the Azure cloud to connect to.
     * Supported types are: AZURE, AZURE_CHINA, AZURE_GERMANY, AZURE_US_GOVERNMENT, OTHER.
     */
    private AzureProfileAware.CloudType cloud;
    /**
     * Properties to Azure Active Directory endpoints.
     */
    private AADProfileEnvironmentProperties environment = new AADProfileEnvironmentProperties();

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public AzureProfileAware.CloudType getCloud() {
        return cloud;
    }

    public void setCloud(AzureProfileAware.CloudType cloud) {
        this.cloud = cloud;
        environment.updatePropertiesByCloudType(cloud);
    }

    public AADProfileEnvironmentProperties getEnvironment() {
        return environment;
    }

    public void setEnvironment(AADProfileEnvironmentProperties environment) {
        this.environment = environment;
    }
}
