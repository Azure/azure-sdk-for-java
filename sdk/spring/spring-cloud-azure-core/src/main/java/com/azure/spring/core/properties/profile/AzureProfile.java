// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.spring.core.aware.AzureProfileAware;

import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE;

/**
 * The AzureProfile defines the properties related to an Azure subscription.
 */
public class AzureProfile extends AzureProfileAdapter {

    private String tenantId;
    private String subscriptionId;
    private AzureProfileAware.CloudType cloud = AZURE;
    private final AzureEnvironment environment = new AzureEnvironment();

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @Override
    public AzureProfileAware.CloudType getCloud() {
        return cloud;
    }

    public void setCloud(AzureProfileAware.CloudType cloud) {
        this.cloud = cloud;
    }

    @Override
    public AzureEnvironment getEnvironment() {
        return environment;
    }

}
