// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.management.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;

import java.util.Objects;

/**
 * Azure profile for client.
 */
public final class AzureProfile {

    private final String tenantId;
    private final String subscriptionId;
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
     */
    public AzureProfile(AzureEnvironment environment) {
        Objects.requireNonNull(environment);
        this.environment = environment;
        Configuration configuration = Configuration.getGlobalConfiguration();
        this.tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        this.subscriptionId = configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
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
     * Gets tenant ID.
     *
     * @return the tenant ID
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * Gets subscription ID.
     *
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Gets Azure environment.
     *
     * @return the Azure environment
     */
    public AzureEnvironment getEnvironment() {
        return environment;
    }
}
