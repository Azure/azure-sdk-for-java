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
    private String tenantId; // tenantId can not set to "common" here, otherwise we can not know whether it's set by customer or it is the default value.
    /**
     * Name of the Azure cloud to connect to.
     * Supported types are: AZURE, AZURE_CHINA, AZURE_GERMANY, AZURE_US_GOVERNMENT, OTHER.
     */
    private AzureProfileAware.CloudType cloud;
    /**
     * Properties to Azure Active Directory endpoints.
     */
    private AADProfileEnvironmentProperties environment = new AADProfileEnvironmentProperties();

    /**
     * Get tenant id.
     *
     * @return tenantId the tenant id
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Set tenant id.
     *
     * @param tenantId the tenant id
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Get cloud.
     *
     * @return cloud the cloud
     */
    public AzureProfileAware.CloudType getCloud() {
        return cloud;
    }

    /**
     * Set tenant id.
     *
     * @param cloud the cloud
     */
    public void setCloud(AzureProfileAware.CloudType cloud) {
        this.cloud = cloud;
        environment.updatePropertiesByCloudType(cloud);
    }

    /**
     * Get environment.
     *
     * @return environment the environment
     */
    public AADProfileEnvironmentProperties getEnvironment() {
        return environment;
    }

    /**
     * Set environment.
     *
     * @param environment the environment
     */
    public void setEnvironment(AADProfileEnvironmentProperties environment) {
        this.environment = environment;
    }
}
