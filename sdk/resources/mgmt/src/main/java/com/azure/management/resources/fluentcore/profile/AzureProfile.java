// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;

public class AzureProfile {

    private String tenantId;
    private String subscriptionId;
    private AzureEnvironment environment;
    private final Configuration configuration = Configuration.getGlobalConfiguration().clone();

    public AzureProfile(AzureEnvironment environment) {
        this(null, null, environment);
    }

    public AzureProfile(String tenantId, String subscriptionId, AzureEnvironment environment) {
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
        this.environment = environment;
    }

    public AzureProfile withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public AzureProfile withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String tenantId() {
        if (this.tenantId == null) {
            this.tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        }
        return this.tenantId;
    }

    public String subscriptionId() {
        if (this.subscriptionId == null) {
            this.subscriptionId = configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
        }
        return this.subscriptionId;
    }

    public AzureEnvironment environment() {
        if (this.environment == null) {
            this.environment = AzureEnvironment.AZURE;
        }
        return environment;
    }
}
