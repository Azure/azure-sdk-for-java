// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.keyvault.fluent.KeyVaultManagementClient;
import com.azure.resourcemanager.keyvault.implementation.KeyVaultManagementClientBuilder;
import com.azure.resourcemanager.keyvault.implementation.VaultsImpl;
import com.azure.resourcemanager.keyvault.models.Vaults;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

/** Entry point to Azure KeyVault resource management. */
public final class KeyVaultManager extends Manager<KeyVaultManagementClient> {
    // Service managers
    private final AuthorizationManager authorizationManager;
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
     * @param profile the profile to use
     * @return the KeyVaultManager
     */
    public static KeyVaultManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(
            HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of KeyVaultManager that exposes KeyVault resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls
     * @param profile the profile to use
     * @return the KeyVaultManager
     */
    private static KeyVaultManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new KeyVaultManager(httpPipeline, profile);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of KeyVaultManager that exposes KeyVault management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing KeyVault management API entry points that work across subscriptions
         */
        KeyVaultManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public KeyVaultManager authenticate(TokenCredential credential, AzureProfile profile) {
            return KeyVaultManager
                .authenticate(
                    buildHttpPipeline(credential, profile), profile);
        }
    }

    private KeyVaultManager(
        final HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new KeyVaultManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
        authorizationManager = AzureConfigurableImpl
            .configureHttpPipeline(httpPipeline, AuthorizationManager.configure())
            .authenticate(null, profile);
        this.tenantId = profile.getTenantId();
    }

    /** @return the KeyVault account management API entry point */
    public Vaults vaults() {
        if (vaults == null) {
            vaults = new VaultsImpl(this, authorizationManager, tenantId);
        }
        return vaults;
    }

//    /**
//     * Creates a new RestClientBuilder instance from the RestClient used by Manager.
//     *
//     * @return the new RestClientBuilder instance created from the RestClient used by Manager
//     */
//    RestClientBuilder newRestClientBuilder() {
//        return restClient.newBuilder();
//    }
}
