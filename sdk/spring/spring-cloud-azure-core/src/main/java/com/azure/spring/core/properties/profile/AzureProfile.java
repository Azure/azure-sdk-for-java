// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.spring.core.aware.AzureProfileAware;

import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE;

/**
 * The AzureProfile defines the properties related to an Azure subscription.
 */
public final class AzureProfile extends AzureProfileAdapter {

    private String tenantId;
    private String subscriptionId;
    private AzureProfileAware.CloudType cloud = AZURE;
    private final AzureEnvironment environment = new KnownAzureEnvironment(AZURE);

    /**
     * @return The tenant id.
     */
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

    /**
     * @return The subscription id.
     */
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

    /**
     * Get the cloud type.
     * @return The cloud type.
     */
    @Override
    public AzureProfileAware.CloudType getCloud() {
        return cloud;
    }

    /**
     * Set the cloud type.
     * @param cloud the cloud type.
     */
    public void setCloud(AzureProfileAware.CloudType cloud) {
        this.cloud = cloud;

        // Explicitly call this method to merge default cloud endpoints to the environment object.
        changeEnvironmentAccordingToCloud();
    }

    /**
     * Get the Azure environment.
     * @return The Azure environment.
     */
    @Override
    public AzureEnvironment getEnvironment() {
        return environment;
    }

}
