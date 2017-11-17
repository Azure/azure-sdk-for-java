/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.keyvault.Vaults;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure KeyVault resource management.
 */
public final class KeyVaultManager extends Manager<KeyVaultManager, KeyVaultManagementClientImpl> {
    // Service managers
    private GraphRbacManager graphRbacManager;
    // Collections
    private Vaults vaults;
    // Variables
    private final String tenantId;

    /**
     * Get a Configurable instance that can be used to create KeyVaultManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new KeyVaultManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of KeyVaultManager that exposes KeyVault resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the KeyVaultManager
     */
    public static KeyVaultManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new KeyVaultManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .withInterceptor(new ResourceManagerThrottlingInterceptor())
                .build(), credentials.domain(), subscriptionId);
    }

    /**
     * Creates an instance of KeyVaultManager that exposes KeyVault resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls
     * @param tenantId the tenant UUID
     * @param subscriptionId the subscription UUID
     * @return the KeyVaultManager
     */
    public static KeyVaultManager authenticate(RestClient restClient, String tenantId, String subscriptionId) {
        return new KeyVaultManager(restClient, tenantId, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of KeyVaultManager that exposes KeyVault management API entry points.
         *
         * @param credentials the credentials to use
         * @param tenantId the tenant UUID
         * @param subscriptionId the subscription UUID
         * @return the interface exposing KeyVault management API entry points that work across subscriptions
         */
        KeyVaultManager authenticate(AzureTokenCredentials credentials, String tenantId, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public KeyVaultManager authenticate(AzureTokenCredentials credentials, String tenantId, String subscriptionId) {
            return KeyVaultManager.authenticate(
                    buildRestClient(credentials, AzureEnvironment.Endpoint.RESOURCE_MANAGER),
                    tenantId, subscriptionId);
        }
    }

    private KeyVaultManager(final RestClient restClient, String tenantId, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new KeyVaultManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
        graphRbacManager = GraphRbacManager.authenticate(restClient, tenantId);
        this.tenantId = tenantId;
    }

    /**
     * @return the KeyVault account management API entry point
     */
    public Vaults vaults() {
        if (vaults == null) {
            vaults = new VaultsImpl(
                    this,
                    graphRbacManager,
                    tenantId);
        }
        return vaults;
    }
}
