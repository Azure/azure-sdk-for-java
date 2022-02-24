// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.azure.spring.core.aware.AzureProfileAware;

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
    private AzureProfileAware.CloudType cloud;
    /**
     * Properties to Azure Active Directory endpoints.
     */
    private AadProfileEnvironmentProperties environment = new AadProfileEnvironmentProperties();

    /**
     *
     * @return The tenant id.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     *
     * @param tenantId The tenant id.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     *
     * @return The clout type.
     */
    public AzureProfileAware.CloudType getCloud() {
        return cloud;
    }

    /**
     *
     * @param cloud The cloud type.
     */
    public void setCloud(AzureProfileAware.CloudType cloud) {
        this.cloud = cloud;
        environment.updatePropertiesByCloudType(cloud);
    }

    /**
     *
     * @return The AADProfileEnvironmentProperties.
     */
    public AadProfileEnvironmentProperties getEnvironment() {
        return environment;
    }

    /**
     *
     * @param environment The AADProfileEnvironmentProperties.
     */
    public void setEnvironment(AadProfileEnvironmentProperties environment) {
        this.environment = environment;
    }
}
