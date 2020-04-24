// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;

/**
 * Azure profile for client.
 */
public class AzureProfile {

    private String tenantId;
    private String subscriptionId;
    private AzureEnvironment environment;
    private final Configuration configuration = Configuration.getGlobalConfiguration().clone();

    /**
     * Creates AzureProfile instance with Azure environment.
     *
     * @param environment the Azure environment
     */
    public AzureProfile(AzureEnvironment environment) {
        this(null, null, environment);
    }

    /**
     * Creates AzureProfile instance with tenant ID, subscription ID and Azure environment.
     *
     * @param tenantId the tenant ID required for Graph Rbac
     * @param subscriptionId the subscription ID required for resource management
     * @param environment the Azure environment
     */
    public AzureProfile(String tenantId, String subscriptionId, AzureEnvironment environment) {
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
        this.environment = environment;
    }

    /**
     * Sets tenant ID to use related services within GraphRbac, AppService, KeyVault.
     *
     * @param tenantId the tenant ID required for Graph Rbac
     * @return the Azure profile
     */
    public AzureProfile withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets subscription ID for resource management.
     *
     * @param subscriptionId the subscription ID
     * @return the Azure profile
     */
    public AzureProfile withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    /**
     * Gets tenant ID.
     *
     * @return the tenant ID
     */
    public String tenantId() {
        if (this.tenantId == null) {
            this.tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        }
        return this.tenantId;
    }

    /**
     * Gets subscription ID.
     *
     * @return the subscription ID
     */
    public String subscriptionId() {
        if (this.subscriptionId == null) {
            this.subscriptionId = configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
        }
        return this.subscriptionId;
    }

    /**
     * Gets Azure environment.
     *
     * @return the Azure environment
     */
    public AzureEnvironment environment() {
        if (this.environment == null) {
            this.environment = AzureEnvironment.AZURE;
        }
        return environment;
    }
}
