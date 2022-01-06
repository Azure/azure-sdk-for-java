// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.profile;

import com.azure.spring.core.aware.AzureProfileAware;
import com.azure.spring.core.properties.profile.AzureEnvironment;
import com.azure.spring.core.properties.profile.AzureProfileAdapter;
import com.azure.spring.core.properties.profile.KnownAzureEnvironment;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * The AzureProfile defines the properties related to an Azure subscription.
 */
public class AzureProfileConfigurationProperties extends AzureProfileAdapter {

    /**
     * Tenant id for Azure resources.
     */
    private String tenantId;
    /**
     * Subscription id to use when connecting to Azure resources.
     */
    private String subscriptionId;
    /**
     * Name of the Azure cloud to connect to.
     */
    private AzureProfileAware.CloudType cloud = AzureProfileAware.CloudType.AZURE;

    @NestedConfigurationProperty
    private final AzureEnvironment environment = new KnownAzureEnvironment(AzureProfileAware.CloudType.AZURE);

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public AzureProfileAware.CloudType getCloud() {
        return cloud;
    }

    public void setCloud(AzureProfileAware.CloudType cloud) {
        this.cloud = cloud;

        // Explicitly call this method to merge default cloud endpoints to the environment object.
        changeEnvironmentAccordingToCloud();
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public AzureEnvironment getEnvironment() {
        return this.environment;
    }

}
