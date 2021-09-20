// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.resource.AppConfigManagedIdentityProperties;
import com.azure.spring.cloud.config.resource.Connection;
import com.azure.spring.cloud.config.resource.ConnectionPool;
import com.azure.spring.cloud.config.stores.ClientStore;

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
     * Creates Connections Pool. Contains basic connection info to each App Configuration Store.
     * 
     * @param properties Configured properties to setup connections.
     * @return ConnectionPool
     */
    @Bean
    public ConnectionPool initConnectionString(AppConfigurationProperties properties) {
        ConnectionPool pool = new ConnectionPool();
        List<ConfigStore> stores = properties.getStores();

        for (ConfigStore store : stores) {
            if (store.isEnabled() && StringUtils.hasText(store.getEndpoint())
                && StringUtils.hasText(store.getConnectionString())) {
                pool.put(store.getEndpoint(), new Connection(store.getConnectionString()));
            } else if (StringUtils.hasText(store.getEndpoint())) {
                AppConfigManagedIdentityProperties msiProps = properties.getManagedIdentity();
                if (msiProps != null && msiProps.getClientId() != null) {
                    pool.put(store.getEndpoint(), new Connection(store.getEndpoint(), msiProps.getClientId()));
                } else {
                    pool.put(store.getEndpoint(), new Connection(store.getEndpoint(), ""));
                }

            }
        }

        Assert.notEmpty(pool.getAll(), "Connection string pool for the configuration stores is empty");

        return pool;
    }

    /**
     * 
     * @param properties Client properties
     * @param appProperties Library properties
     * @param clients Store Connections
     * @param keyVaultCredentialProviderOptional Optional credentials for connecting to KeyVault
     * @param keyVaultClientProviderOptional Optional client for connecting to Key Vault
     * @param keyVaultSecretProviderOptional Secret Resolver
     * @return App Configuration Property Source Locator
     * @throws IllegalArgumentException if both KeyVaultClientProvider and KeyVaultSecretProvider exist.
     */
    @Bean
    public AppConfigurationPropertySourceLocator sourceLocator(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, ClientStore clients, 
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

        return new AppConfigurationPropertySourceLocator(properties, appProperties, clients,
            keyVaultCredentialProvider, keyVaultClientProvider, keyVaultSecretProvider);
    }

    /**
     * Builds ClientStores used for connecting to App Configuration.
     * 
     * @param properties Client configurations for setting up connections to each config store.
     * @param appProperties Library configurations for setting up connections to each config store.
     * @param pool Basic connection info for connecting to each config store.
     * @param tokenCredentialProviderOptional Optional provider for overriding Token Credentials for connecting to App Configuration.
     * @param clientProviderOptional Optional client for overriding Client Connections to App Configuration stores.
     * @return ClientStore
     */
    @Bean
    public ClientStore buildClientStores(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, ConnectionPool pool,
        Optional<AppConfigurationCredentialProvider> tokenCredentialProviderOptional,
        Optional<ConfigurationClientBuilderSetup> clientProviderOptional) {

        AppConfigurationCredentialProvider tokenCredentialProvider = null;
        ConfigurationClientBuilderSetup clientProvider = null;

        if (!tokenCredentialProviderOptional.isPresent()) {
            LOGGER.debug("No AppConfigurationCredentialProvider found.");
        } else {
            tokenCredentialProvider = tokenCredentialProviderOptional.get();
        }

        if (!clientProviderOptional.isPresent()) {
            LOGGER.debug("No AppConfigurationClientProvider found.");
        } else {
            clientProvider = clientProviderOptional.get();
        }

        return new ClientStore(appProperties, pool, tokenCredentialProvider, clientProvider);
    }
}
