// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.azure.spring.core.aware.AzureProfileOptionsAware;

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
    private AzureProfileOptionsAware.CloudType cloudType;
    /**
     * Properties to Azure Active Directory endpoints.
     */
    private AADProfileEnvironmentProperties environment = new AADProfileEnvironmentProperties();

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
     * @return The cloud type.
     */
    public AzureProfileOptionsAware.CloudType getCloudType() {
        return cloudType;
    }

    /**
     *
     * @param cloudType The cloud type.
     */
    public void setCloudType(AzureProfileOptionsAware.CloudType cloudType) {
        this.cloudType = cloudType;
        environment.updatePropertiesByCloudType(cloudType);
    }

    /**
     *
     * @return The AADProfileEnvironmentProperties.
     */
    public AADProfileEnvironmentProperties getEnvironment() {
        return environment;
    }

    /**
     *
     * @param environment The AADProfileEnvironmentProperties.
     */
    public void setEnvironment(AADProfileEnvironmentProperties environment) {
        this.environment = environment;
    }
}
