// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.spring.core.aware.AzureProfileAware;

/**
 * The AzureProfile defines the properties related to an Azure subscription.
 */
public class AzureProfile implements AzureProfileAware.Profile {

    private String tenantId;
    private String subscriptionId;
    private AzureProfileAware.CloudType cloud = AzureProfileAware.CloudType.AZURE;
    private final AzureEnvironment otherEnvironment = new AzureEnvironment(com.azure.core.management.AzureEnvironment.AZURE);

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

    public AzureEnvironment getEnvironment() {
        switch (cloud) {
            case AZURE_CHINA:
                return KnownAzureEnvironment.AZURE_CHINA_ENV;
            case AZURE_US_GOVERNMENT:
                return KnownAzureEnvironment.AZURE_US_GOVERNMENT_ENV;
            case AZURE_GERMANY:
                return KnownAzureEnvironment.AZURE_GERMANY_ENV;
            case AZURE:
                return KnownAzureEnvironment.AZURE_ENV;
            default:
                return otherEnvironment;
        }
    }





}
