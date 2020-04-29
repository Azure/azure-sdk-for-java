// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;

import java.util.Objects;

/**
 * Azure profile for client.
 */
public class AzureProfile {

    private String tenantId;
    private String subscriptionId;
    private final AzureEnvironment environment;

    /**
     * Creates AzureProfile instance with Azure environment. The global environment is {@link AzureEnvironment#AZURE}.
     * The tenant ID and subscription ID can be set via environment variables. The environment variables are expected
     * as below:
     * <ul>
     *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
     *     <li>{@link Configuration#PROPERTY_AZURE_SUBSCRIPTION_ID AZURE_SUBSCRIPTION_ID}</li>
     * </ul>
     *
     * @param environment the Azure environment
     * @param loadEnvironmentVariables the boolean flag indicates whether the environment variables are set
     */
    public AzureProfile(AzureEnvironment environment, boolean loadEnvironmentVariables) {
        Objects.requireNonNull(environment);
        this.environment = environment;
        if (loadEnvironmentVariables) {
            Configuration configuration = Configuration.getGlobalConfiguration();
            this.tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
            this.subscriptionId = configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
        }
    }

    /**
     * Creates AzureProfile instance with tenant ID, subscription ID and Azure environment.
     * The global environment is {@link AzureEnvironment#AZURE}.
     *
     * @param tenantId the tenant ID required for Graph Rbac
     * @param subscriptionId the subscription ID required for resource management
     * @param environment the Azure environment
     */
    public AzureProfile(String tenantId, String subscriptionId, AzureEnvironment environment) {
        Objects.requireNonNull(environment);
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
        return this.tenantId;
    }

    /**
     * Gets subscription ID.
     *
     * @return the subscription ID
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Gets Azure environment.
     *
     * @return the Azure environment
     */
    public AzureEnvironment environment() {
        return environment;
    }
}
