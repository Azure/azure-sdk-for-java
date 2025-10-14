// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.management.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.models.AzureCloud;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Azure profile for client.
 */
public final class AzureProfile {
    private static final ClientLogger LOGGER = new ClientLogger(AzureProfile.class);

    private final String tenantId;
    private final String subscriptionId;
    private final AzureEnvironment environment;
    private static final Map<AzureCloud, AzureEnvironment> ENDPOINT_MAP = new HashMap<>();

    static {
        ENDPOINT_MAP.put(AzureCloud.AZURE_PUBLIC_CLOUD, AzureEnvironment.AZURE);
        ENDPOINT_MAP.put(AzureCloud.AZURE_CHINA_CLOUD, AzureEnvironment.AZURE_CHINA);
        ENDPOINT_MAP.put(AzureCloud.AZURE_US_GOVERNMENT_CLOUD, AzureEnvironment.AZURE_US_GOVERNMENT);
    }

    /**
     * Creates AzureProfile instance with specific Azure cloud. The global cloud is {@link AzureCloud#AZURE_PUBLIC_CLOUD}.
     * The tenant ID and subscription ID can be set via environment variables. The environment variables are expected
     * as below:
     * <ul>
     *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
     *     <li>{@link Configuration#PROPERTY_AZURE_SUBSCRIPTION_ID AZURE_SUBSCRIPTION_ID}</li>
     * </ul>
     *
     * @param azureCloud the Azure cloud
     */
    public AzureProfile(AzureCloud azureCloud) {
        Objects.requireNonNull(azureCloud);
        this.environment = fromAzureCloud(azureCloud);
        Configuration configuration = Configuration.getGlobalConfiguration();
        this.tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        this.subscriptionId = configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
    }

    /**
     * Creates AzureProfile instance with tenant ID, subscription ID and specific Azure cloud.
     * The global cloud is {@link AzureCloud#AZURE_PUBLIC_CLOUD}.
     *
     * @param tenantId the tenant ID required for Graph Rbac
     * @param subscriptionId the subscription ID required for resource management
     * @param azureCloud the Azure cloud
     */
    public AzureProfile(String tenantId, String subscriptionId, AzureCloud azureCloud) {
        Objects.requireNonNull(azureCloud);
        this.environment = fromAzureCloud(azureCloud);
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
    }

    /**
     * <p>Note: Only use this constructor for custom Azure cloud/endpoints.
     * Use {@link AzureProfile#AzureProfile(String, String, AzureCloud)} for global environment.</p>
     * Creates AzureProfile instance with Azure environment. The global environment is {@link AzureEnvironment#AZURE}.
     * The tenant ID and subscription ID can be set via environment variables. The environment variables are expected
     * as below:
     * <ul>
     *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
     *     <li>{@link Configuration#PROPERTY_AZURE_SUBSCRIPTION_ID AZURE_SUBSCRIPTION_ID}</li>
     * </ul>
     *
     * @param environment the Azure environment
     * @see AzureProfile#AzureProfile(AzureCloud)
     */
    public AzureProfile(AzureEnvironment environment) {
        Objects.requireNonNull(environment);
        this.environment = environment;
        Configuration configuration = Configuration.getGlobalConfiguration();
        this.tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        this.subscriptionId = configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
    }

    /**
     * <p>Note: Only use this constructor for custom Azure cloud/endpoints.
     * Use {@link AzureProfile#AzureProfile(String, String, AzureCloud)} for global environment.</p>
     * Creates AzureProfile instance with tenant ID, subscription ID and Azure environment.
     * The global environment is {@link AzureEnvironment#AZURE}.
     *
     * @param tenantId the tenant ID required for Graph Rbac
     * @param subscriptionId the subscription ID required for resource management
     * @param environment the Azure environment
     * @see AzureProfile#AzureProfile(String, String, AzureCloud)
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

    private AzureEnvironment fromAzureCloud(AzureCloud azureCloud) {
        AzureEnvironment azureEnvironment = ENDPOINT_MAP.get(azureCloud);
        if (azureEnvironment == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("No endpoint mapping defined for AzureCloud: [%s].", azureCloud)));
        }
        return azureEnvironment;
    }
}
