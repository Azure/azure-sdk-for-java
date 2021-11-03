// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.profile;

import com.azure.spring.core.aware.AzureProfileAware;
import com.azure.spring.core.properties.profile.AzureEnvironment;

/**
 * The AzureProfile defines the properties related to an Azure subscription.
 */
public class AzureProfileCP implements AzureProfileAware.Profile {

    private String tenantId;
    private String subscriptionId;
    private String cloud = "Azure"; // TODO (xiada) this name
    private AzureEnvironment environment = AzureEnvironment.AZURE;

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

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
        this.environment = AzureEnvironment.fromAzureCloud(cloud);
    }

    public AzureEnvironment getEnvironment() {
        return environment;
    }

}
