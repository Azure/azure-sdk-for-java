// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.stores;

import java.net.URI;
import java.time.Duration;

import org.springframework.util.StringUtils;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;

/**
 * Client for connecting to and getting secrets from a Key Vault
 */
public final class AppConfigurationSecretClientManager {

    private SecretAsyncClient secretClient;

    private final SecretClientCustomizer keyVaultClientProvider;

    private final String endpoint;

    private final KeyVaultSecretProvider keyVaultSecretProvider;

    private final SecretClientBuilderFactory secretClientFactory;

    private final boolean credentialConfigured;
    
    private final int timeout = 30;

    /**
     * Creates a Client for connecting to Key Vault
     * @param endpoint Key Vault endpoint
     * @param keyVaultClientProvider optional provider for overriding the Key Vault Client
     * @param keyVaultSecretProvider optional provider for providing Secrets instead of connecting to Key Vault
     * @param secretClientFactory Factory for building clients to Key Vault
     * @param credentialConfigured Is a credential configured with Global Configurations or Service Configurations
     */
    public AppConfigurationSecretClientManager(String endpoint, SecretClientCustomizer keyVaultClientProvider,
        KeyVaultSecretProvider keyVaultSecretProvider, SecretClientBuilderFactory secretClientFactory,
        boolean credentialConfigured) {
        this.endpoint = endpoint;
        this.keyVaultClientProvider = keyVaultClientProvider;
        this.keyVaultSecretProvider = keyVaultSecretProvider;
        this.secretClientFactory = secretClientFactory;
        this.credentialConfigured = credentialConfigured;
    }

    AppConfigurationSecretClientManager build() {
        SecretClientBuilder builder = secretClientFactory.build();

        if (!credentialConfigured) {
            // System Assigned Identity.
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        builder.vaultUrl(endpoint);

        if (keyVaultClientProvider != null) {
            keyVaultClientProvider.customize(builder, endpoint);
        }

        secretClient = builder.buildAsyncClient();

        return this;
    }

    /**
     * Gets the specified secret using the Secret Identifier
     *
     * @param secretIdentifier The Secret Identifier to Secret
     * @param timeout How long it waits for a response from Key Vault
     * @return Secret values that matches the secretIdentifier
     */
    public KeyVaultSecret getSecret(URI secretIdentifier) {
        if (secretClient == null) {
            build();
        }

        String[] tokens = secretIdentifier.getPath().split("/");

        String name = (tokens.length >= 3 ? tokens[2] : null);
        String version = (tokens.length >= 4 ? tokens[3] : null);

        if (keyVaultSecretProvider != null) { // Secret Resolver
            String secret = keyVaultSecretProvider.getSecret(secretIdentifier.getRawPath());
            if (StringUtils.hasText(secret)) {
                return new KeyVaultSecret(name, secret);
            }
        }

        return secretClient.getSecret(name, version).block(Duration.ofSeconds(timeout));
    }

}
