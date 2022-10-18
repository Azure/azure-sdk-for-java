// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.util.HashMap;
import java.util.Map;

import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.config.SecretClientBuilderSetup;
import com.azure.spring.cloud.config.implementation.stores.AppConfigurationSecretClientManager;

public class AppConfigurationKeyVaultClientFactory {

    private final Map<String, AppConfigurationSecretClientManager> keyVaultClients;

    private final KeyVaultCredentialProvider keyVaultCredentialProvider;

    private final SecretClientBuilderSetup keyVaultClientProvider;

    private final KeyVaultSecretProvider keyVaultSecretProvider;

    private final String authClientId;

    public AppConfigurationKeyVaultClientFactory(KeyVaultCredentialProvider keyVaultCredentialProvider,
        SecretClientBuilderSetup keyVaultClientProvider, KeyVaultSecretProvider keyVaultSecretProvider,
        String authClientId) {
        this.keyVaultClientProvider = keyVaultClientProvider;
        this.keyVaultCredentialProvider = keyVaultCredentialProvider;
        this.keyVaultSecretProvider = keyVaultSecretProvider;
        this.authClientId = authClientId;
        keyVaultClients = new HashMap<>();
    }

    public AppConfigurationSecretClientManager getClient(String host) {
        // Check if we already have a client for this key vault, if not we will make
        // one
        if (!keyVaultClients.containsKey(host)) {
            AppConfigurationSecretClientManager client = new AppConfigurationSecretClientManager(host,
                keyVaultCredentialProvider, keyVaultClientProvider, keyVaultSecretProvider, authClientId);
            keyVaultClients.put(host, client);
        }
        return keyVaultClients.get(host);
    }

}
