// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.profile;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

/**
 * The AzureProfile defines the properties related to an Azure subscription.
 */
public final class AzureProfileProperties extends AzureProfileOptionsAdapter {

    private String tenantId;
    private String subscriptionId;
    private AzureProfileOptionsProvider.CloudType cloudType;
    private final AzureEnvironmentProperties environment;

    /**
     * Construct an {@link AzureProfileProperties} instance with default value.
     */
    public AzureProfileProperties() {
        environment = new AzureEnvironmentProperties();
        setCloudType(AzureProfileOptionsProvider.CloudType.AZURE);
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Set the tenant id.
     * @param tenantId The tenant id.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Set the subscription id.
     * @param subscriptionId The subscription id.
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @Override
    public AzureProfileOptionsProvider.CloudType getCloudType() {
        return cloudType;
    }

    /**
     * Set the cloud type.
     * @param cloudType the cloud type.
     */
    public void setCloudType(AzureProfileOptionsProvider.CloudType cloudType) {
        this.cloudType = cloudType;

        // Explicitly call this method to merge default cloud endpoints to the environment object.
        changeEnvironmentAccordingToCloud();
    }

    @Override
    public AzureEnvironmentProperties getEnvironment() {
        return environment;
    }

}
