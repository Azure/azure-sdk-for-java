// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.HashMap;
import java.util.Map;

import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.stores.AppConfigurationSecretClientManager;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;

public class AppConfigurationKeyVaultClientFactory {

    private final Map<String, AppConfigurationSecretClientManager> keyVaultClients;

    private final SecretClientCustomizer keyVaultClientProvider;

    private final KeyVaultSecretProvider keyVaultSecretProvider;

    private final SecretClientBuilderFactory secretClientFactory;
    
    private final boolean credentialsConfigured;

    private final boolean isConfigured;

    public AppConfigurationKeyVaultClientFactory(SecretClientCustomizer keyVaultClientProvider,
        KeyVaultSecretProvider keyVaultSecretProvider, SecretClientBuilderFactory secretClientFactory,
        boolean credentialsConfigured) {
        this.keyVaultClientProvider = keyVaultClientProvider;
        this.keyVaultSecretProvider = keyVaultSecretProvider;
        this.secretClientFactory = secretClientFactory;
        keyVaultClients = new HashMap<>();
        this.credentialsConfigured = credentialsConfigured;
        isConfigured = keyVaultClientProvider != null || credentialsConfigured;
    }

    public AppConfigurationSecretClientManager getClient(String host) {
        // Check if we already have a client for this key vault, if not we will make
        // one
        if (!keyVaultClients.containsKey(host)) {
            AppConfigurationSecretClientManager client = new AppConfigurationSecretClientManager(host,
                keyVaultClientProvider, keyVaultSecretProvider, secretClientFactory, credentialsConfigured);
            keyVaultClients.put(host, client);
        }
        return keyVaultClients.get(host);
    }

    public boolean isConfigured() {
        return isConfigured;
    }
}
