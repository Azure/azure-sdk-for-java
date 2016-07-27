/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.management.keyvault.Vaults;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.StorageAccountsImpl;
import com.microsoft.azure.management.storage.implementation.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Entry point to Azure storage resource management.
 */
public final class KeyVaultManager extends Manager<KeyVaultManager, KeyVaultManagementClientImpl> {
    // Collections
    private Vaults vaults;

    /**
     * Get a Configurable instance that can be used to create StorageManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new KeyVaultManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static KeyVaultManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new KeyVaultManager(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static KeyVaultManager authenticate(RestClient restClient, String subscriptionId) {
        return new KeyVaultManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of StorageManager that exposes storage management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing storage management API entry points that work across subscriptions
         */
        KeyVaultManager authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public KeyVaultManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
            return KeyVaultManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private KeyVaultManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new KeyVaultManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
        }

    /**
     * @return the storage account management API entry point
     */
    public Vaults vaults() {
        if (vaults == null) {
            vaults = new VaultsImpl(
                    super.innerManagementClient.vaults(),
                    this);
        }
        return vaults;
    }
}
