// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.stores;

import java.net.URI;
import java.time.Duration;

import org.apache.commons.lang3.StringUtils;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.SecretClientBuilderSetup;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.resource.AppConfigManagedIdentityProperties;

/**
 * Client for connecting to and getting secrets from a Key Vault
 */
public class KeyVaultClient {

    private SecretAsyncClient secretClient;

    private final AppConfigurationProperties properties;

    private final SecretClientBuilderSetup keyVaultClientProvider;

    private final URI uri;

    private final TokenCredential tokenCredential;

    public KeyVaultClient(
        AppConfigurationProperties properties,
        URI uri,
        KeyVaultCredentialProvider tokenCredentialProvider,
        SecretClientBuilderSetup keyVaultClientProvider
    ) {
        this.properties = properties;
        this.uri = uri;
        if (tokenCredentialProvider != null) {
            this.tokenCredential = tokenCredentialProvider.getKeyVaultCredential("https://" + uri.getHost());
        } else {
            this.tokenCredential = null;
        }
        this.keyVaultClientProvider = keyVaultClientProvider;
    }

    KeyVaultClient build() {
        SecretClientBuilder builder = getBuilder();
        AppConfigManagedIdentityProperties msiProps = properties.getManagedIdentity();
        String fullUri = "https://" + uri.getHost();

        if (tokenCredential != null && msiProps != null) {
            throw new IllegalArgumentException("More than 1 Conncetion method was set for connecting to Key Vault.");
        }

        if (tokenCredential != null) {
            // User Provided Token Credential
            builder.credential(tokenCredential);
        } else if (msiProps != null && StringUtils.isNotEmpty(msiProps.getClientId())) {
            // User Assigned Identity - Client ID through configuration file.
            builder.credential(new ManagedIdentityCredentialBuilder().clientId(msiProps.getClientId()).build());
        } else {
            // System Assigned Identity.
            builder.credential(new ManagedIdentityCredentialBuilder().build());
        }
        builder.vaultUrl(fullUri);

        if (keyVaultClientProvider != null) {
            keyVaultClientProvider.setup(builder, fullUri);
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
    public KeyVaultSecret getSecret(URI secretIdentifier, int timeout) {
        if (secretClient == null) {
            build();
        }
        String[] tokens = secretIdentifier.getPath().split("/");

        String name = (tokens.length >= 3 ? tokens[2] : null);
        String version = (tokens.length >= 4 ? tokens[3] : null);
        return secretClient.getSecret(name, version).block(Duration.ofSeconds(timeout));
    }

    SecretClientBuilder getBuilder() {
        return new SecretClientBuilder();
    }

}
