/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.keyvault.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.keyvault.Vaults;
import com.azure.management.keyvault.models.KeyVaultManagementClientBuilder;
import com.azure.management.keyvault.models.KeyVaultManagementClientImpl;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;

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
     * @param credential the credential to use
     * @param subscriptionId the subscription UUID
     * @return the KeyVaultManager
     */
    public static KeyVaultManager authenticate(AzureTokenCredential credential, String subscriptionId) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), credential.getDomain(), subscriptionId);
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
        return authenticate(restClient, tenantId,subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of KeyVaultManager that exposes KeyVault resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls
     * @param tenantId the tenant UUID
     * @param subscriptionId the subscription UUID
     * @param sdkContext the sdk context
     * @return the KeyVaultManager
     */
    public static KeyVaultManager authenticate(RestClient restClient, String tenantId, String subscriptionId, SdkContext sdkContext) {
        return new KeyVaultManager(restClient, tenantId, subscriptionId, sdkContext);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of KeyVaultManager that exposes KeyVault management API entry points.
         *
         * @param credential the credential to use
         * @param tenantId the tenant UUID
         * @param subscriptionId the subscription UUID
         * @return the interface exposing KeyVault management API entry points that work across subscriptions
         */
        KeyVaultManager authenticate(AzureTokenCredential credential, String tenantId, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public KeyVaultManager authenticate(AzureTokenCredential credential, String tenantId, String subscriptionId) {
            return KeyVaultManager.authenticate(
                    buildRestClient(credential, AzureEnvironment.Endpoint.RESOURCE_MANAGER),
                    tenantId, subscriptionId);
        }
    }

    private KeyVaultManager(final RestClient restClient, String tenantId, String subscriptionId, SdkContext sdkContext) {
        super(
                restClient,
                subscriptionId,
                new KeyVaultManagementClientBuilder()
                        .pipeline(restClient.getHttpPipeline())
                        .host(restClient.getBaseUrl().toString())
                        .subscriptionId(subscriptionId)
                        .build(),
                sdkContext);
        graphRbacManager = GraphRbacManager.authenticate(restClient, tenantId, sdkContext);
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

    /**
     * Creates a new RestClientBuilder instance from the RestClient used by Manager.
     *
     * @return the new RestClientBuilder instance created from the RestClient used by Manager
     */
    RestClientBuilder newRestClientBuilder() {
        return restClient.newBuilder();
    }
}
