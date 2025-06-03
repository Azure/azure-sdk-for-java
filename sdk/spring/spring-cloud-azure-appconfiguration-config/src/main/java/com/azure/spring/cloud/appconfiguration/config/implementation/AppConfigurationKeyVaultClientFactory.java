// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.HashMap;
import java.util.Map;

import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.stores.AppConfigurationSecretClientManager;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;

/**
 * Factory for creating and managing AppConfigurationSecretClientManager instances. This class caches clients per Key
 * Vault host.
 */
class AppConfigurationKeyVaultClientFactory {
    /**
     * Cache of secret client managers by Key Vault host.
     */
    private final Map<String, AppConfigurationSecretClientManager> keyVaultClients;

    /**
     * Optional customizer for Key Vault secret clients.
     */
    private final SecretClientCustomizer keyVaultClientProvider;

    /**
     * Optional provider for custom secret resolution.
     */
    private final KeyVaultSecretProvider keyVaultSecretProvider;

    /**
     * Factory for creating secret client builders.
     */
    private final SecretClientBuilderFactory secretClientFactory;

    /**
     * Flag indicating whether credentials are configured.
     */
    private final boolean credentialsConfigured;

    /**
     * Flag indicating whether the factory being used for telemetry.
     */
    private final boolean isConfigured;

    /**
     * Creates a new AppConfigurationKeyVaultClientFactory.
     * 
     * @param keyVaultClientProvider optional customizer for Key Vault secret clients
     * @param keyVaultSecretProvider optional provider for custom secret resolution
     * @param secretClientFactory factory for creating secret client builders
     * @param credentialsConfigured whether credentials are configured
     */
    AppConfigurationKeyVaultClientFactory(SecretClientCustomizer keyVaultClientProvider,
        KeyVaultSecretProvider keyVaultSecretProvider, SecretClientBuilderFactory secretClientFactory,
        boolean credentialsConfigured) {
        this.keyVaultClientProvider = keyVaultClientProvider;
        this.keyVaultSecretProvider = keyVaultSecretProvider;
        this.secretClientFactory = secretClientFactory;
        keyVaultClients = new HashMap<>();
        this.credentialsConfigured = credentialsConfigured;
        isConfigured = keyVaultClientProvider != null || credentialsConfigured;
    }

    /**
     * Gets or creates a secret client manager for the specified Key Vault host.
     * 
     * @param host the Key Vault host endpoint
     * @return the secret client manager for the host
     */
    AppConfigurationSecretClientManager getClient(String host) {
        // Check if we already have a client for this key vault, if not we will make
        // one
        if (!keyVaultClients.containsKey(host)) {
            AppConfigurationSecretClientManager client = new AppConfigurationSecretClientManager(host,
                keyVaultClientProvider, keyVaultSecretProvider, secretClientFactory, credentialsConfigured);
            keyVaultClients.put(host, client);
        }
        return keyVaultClients.get(host);
    }

    /**
     * Returns if Key Vault is configured to be used.
     * 
     * @return true if either a client provider is configured or credentials are configured
     */
    boolean isConfigured() {
        return isConfigured;
    }
}
