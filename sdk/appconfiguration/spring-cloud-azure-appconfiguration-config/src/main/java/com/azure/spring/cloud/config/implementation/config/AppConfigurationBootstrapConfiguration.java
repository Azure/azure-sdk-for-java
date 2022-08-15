// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.config.AppConfigurationCredentialProvider;
import com.azure.spring.cloud.config.ConfigurationClientBuilderSetup;
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.config.SecretClientBuilderSetup;
import com.azure.spring.cloud.config.implementation.AppConfigurationPropertySourceLocator;
import com.azure.spring.cloud.config.implementation.AppConfigurationReplicaClientsBuilder;
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.implementation.AppConfigurationReplicaClientFactory;

/**
 * Setup ConnectionPool, AppConfigurationPropertySourceLocator, and ClientStore when
 * <i>spring.cloud.azure.appconfiguration.enabled</i> is enabled.
 */
@Configuration
@EnableConfigurationProperties({ AppConfigurationProperties.class, AppConfigurationProviderProperties.class })
@ConditionalOnClass(AppConfigurationPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AppConfigurationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AppConfigurationBootstrapConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationBootstrapConfiguration.class);

    /**
     *
     * @param properties Client properties
     * @param appProperties Library properties
     * @param clientFactory Store Connections
     * @param keyVaultCredentialProviderOptional Optional credentials for connecting to KeyVault
     * @param keyVaultClientProviderOptional Optional client for connecting to Key Vault
     * @param keyVaultSecretProviderOptional Secret Resolver
     * @return AppConfigurationPropertySourceLocator
     * @throws IllegalArgumentException if both KeyVaultClientProvider and KeyVaultSecretProvider exist.
     */
    @Bean
    AppConfigurationPropertySourceLocator sourceLocator(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, AppConfigurationReplicaClientFactory clientFactory,
        Optional<KeyVaultCredentialProvider> keyVaultCredentialProviderOptional,
        Optional<SecretClientBuilderSetup> keyVaultClientProviderOptional,
        Optional<KeyVaultSecretProvider> keyVaultSecretProviderOptional) throws IllegalArgumentException {

        KeyVaultCredentialProvider keyVaultCredentialProvider = null;
        SecretClientBuilderSetup keyVaultClientProvider = null;
        KeyVaultSecretProvider keyVaultSecretProvider = null;

        if (!keyVaultCredentialProviderOptional.isPresent()) {
            LOGGER.debug("No KeyVaultCredentialProvider found.");
        } else {
            keyVaultCredentialProvider = keyVaultCredentialProviderOptional.get();
        }

        if (!keyVaultClientProviderOptional.isPresent()) {
            LOGGER.debug("No KeyVaultCredentialProvider found.");
        } else {
            keyVaultClientProvider = keyVaultClientProviderOptional.get();
        }

        if (!keyVaultSecretProviderOptional.isPresent()) {
            LOGGER.debug("No KeyVaultSecretProvider found.");
        } else {
            keyVaultSecretProvider = keyVaultSecretProviderOptional.get();
        }

        if (keyVaultClientProvider != null && keyVaultSecretProvider != null) {
            throw new IllegalArgumentException(
                "KeyVaultClientProvider and KeyVaultSecretProvider both can't have Beans supplied.");
        }

        return new AppConfigurationPropertySourceLocator(properties, appProperties, clientFactory,
            keyVaultCredentialProvider, keyVaultClientProvider, keyVaultSecretProvider);
    }

    /**
     * Factory for working with App Configuration Clients
     *
     * @param clientBuilder Builder for configuration clients
     * @param properties Client configurations for setting up connections to each config store.
     * @param appProperties Library configurations for setting up connections to each config store.
     * @return AppConfigurationReplicaClientFactory
     */
    @Bean
    @ConditionalOnMissingBean
    AppConfigurationReplicaClientFactory buildClientFactory(AppConfigurationReplicaClientsBuilder clientBuilder,
        AppConfigurationProperties properties, AppConfigurationProviderProperties appProperties) {
        return new AppConfigurationReplicaClientFactory(clientBuilder, properties, appProperties);
    }

    /**
     * Builder for clients connecting to App Configuration.
     *
     * @param properties Client configurations for setting up connections to each config store.
     * @param appProperties Library configurations for setting up connections to each config store.
     * @param tokenCredentialProviderOptional Optional provider for overriding Token Credentials for connecting to App
     * Configuration.
     * @param clientProviderOptional Optional client for overriding Client Connections to App Configuration stores.
     * @param keyVaultCredentialProviderOptional optional provider, used to see if Key Vault is configured
     * @param keyVaultClientProviderOptional optional client, used to see if Key Vault is configured
     * @return ClientStore
     */
    @Bean
    @ConditionalOnMissingBean
    AppConfigurationReplicaClientsBuilder replicaClientBuilder(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties,
        Optional<AppConfigurationCredentialProvider> tokenCredentialProviderOptional,
        Optional<ConfigurationClientBuilderSetup> clientProviderOptional,
        Optional<KeyVaultCredentialProvider> keyVaultCredentialProviderOptional,
        Optional<SecretClientBuilderSetup> keyVaultClientProviderOptional) {

        AppConfigurationReplicaClientsBuilder clientBuilder = new AppConfigurationReplicaClientsBuilder(
            appProperties.getMaxRetries());

        if (!tokenCredentialProviderOptional.isPresent()) {
            LOGGER.debug("No AppConfigurationCredentialProvider found.");
        } else {
            clientBuilder.setTokenCredentialProvider(tokenCredentialProviderOptional.get());
        }

        if (!clientProviderOptional.isPresent()) {
            LOGGER.debug("No AppConfigurationClientProvider found.");
        } else {
            clientBuilder.setClientProvider(clientProviderOptional.get());
        }

        if (keyVaultCredentialProviderOptional.isPresent() || keyVaultClientProviderOptional.isPresent()) {
            clientBuilder.setKeyVaultConfigured(true);
        }
        
        clientBuilder.setClientId(properties.getClientId());

        return clientBuilder;
    }
}
