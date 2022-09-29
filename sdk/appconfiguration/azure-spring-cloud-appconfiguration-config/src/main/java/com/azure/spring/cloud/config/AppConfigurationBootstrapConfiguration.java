// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.config.implementation.AppConfigurationPropertySourceLocator;
import com.azure.spring.cloud.config.implementation.AppConfigurationReplicaClientFactory;
import com.azure.spring.cloud.config.implementation.AppConfigurationReplicaClientsBuilder;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

/**
 * Setup ConnectionPool, AppConfigurationPropertySourceLocator, and ClientStore when
 * <i>spring.cloud.azure.appconfiguration.enabled</i> is enabled.
 */
@Configuration
@EnableConfigurationProperties({ AppConfigurationProperties.class, AppConfigurationProviderProperties.class })
@ConditionalOnClass(AppConfigurationPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AppConfigurationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AppConfigurationBootstrapConfiguration {

    @Autowired
    private transient ApplicationContext context;

    /**
     *
     * @param properties Client properties
     * @param appProperties Library properties
     * @param clientFactory Store Connections
     * 
     * @return AppConfigurationPropertySourceLocator
     * @throws IllegalArgumentException if both KeyVaultClientProvider and KeyVaultSecretProvider exist.
     */
    @Bean
    AppConfigurationPropertySourceLocator sourceLocator(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, AppConfigurationReplicaClientFactory clientFactory)
        throws IllegalArgumentException {

        KeyVaultCredentialProvider keyVaultCredentialProvider = context
            .getBeanProvider(KeyVaultCredentialProvider.class).getIfAvailable();
        SecretClientBuilderSetup keyVaultClientProvider = context.getBeanProvider(SecretClientBuilderSetup.class)
            .getIfAvailable();
        KeyVaultSecretProvider keyVaultSecretProvider = context.getBeanProvider(KeyVaultSecretProvider.class)
            .getIfAvailable();

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
     * @return AppConfigurationReplicaClientFactory
     */
    @Bean
    @ConditionalOnMissingBean
    AppConfigurationReplicaClientFactory replicaClientFactory(AppConfigurationReplicaClientsBuilder clientBuilder,
        AppConfigurationProperties properties) {
        return new AppConfigurationReplicaClientFactory(clientBuilder, properties);
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
        AppConfigurationProviderProperties appProperties) {

        AppConfigurationReplicaClientsBuilder clientBuilder = new AppConfigurationReplicaClientsBuilder(
            appProperties.getMaxRetries());

        clientBuilder.setTokenCredentialProvider(
            context.getBeanProvider(AppConfigurationCredentialProvider.class).getIfAvailable());
        clientBuilder
            .setClientProvider(context.getBeanProvider(ConfigurationClientBuilderSetup.class).getIfAvailable());

        KeyVaultCredentialProvider keyVaultCredentialProvider = context
            .getBeanProvider(KeyVaultCredentialProvider.class).getIfAvailable();
        SecretClientBuilderSetup keyVaultClientProvider = context.getBeanProvider(SecretClientBuilderSetup.class)
            .getIfAvailable();

        if (keyVaultCredentialProvider != null || keyVaultClientProvider != null) {
            clientBuilder.setKeyVaultConfigured(true);
        }

        if (properties.getManagedIdentity() != null) {
            clientBuilder.setClientId(properties.getManagedIdentity().getClientId());
        }

        return clientBuilder;
    }
}
