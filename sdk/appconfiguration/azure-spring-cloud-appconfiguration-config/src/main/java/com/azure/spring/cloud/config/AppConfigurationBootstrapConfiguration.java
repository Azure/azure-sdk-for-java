// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.azure.spring.cloud.config.implementation.AppConfigurationPropertySourceLocator;
import com.azure.spring.cloud.config.implementation.ClientFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationBootstrapConfiguration.class);

    /**
     *
     * @param properties Client properties
     * @param appProperties Library properties
     * @param clientFactory Store Connections
     * @param keyVaultCredentialProviderOptional Optional credentials for connecting to KeyVault
     * @param keyVaultClientProviderOptional Optional client for connecting to Key Vault
     * @param keyVaultSecretProviderOptional Secret Resolver
     * @return App Configuration Property Source Locator
     * @throws IllegalArgumentException if both KeyVaultClientProvider and KeyVaultSecretProvider exist.
     */
    @Bean
    AppConfigurationPropertySourceLocator sourceLocator(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, ClientFactory clientFactory,
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
     * Builds ClientStores used for connecting to App Configuration.
     *
     * @param properties Client configurations for setting up connections to each config store.
     * @param appProperties Library configurations for setting up connections to each config store.
     * @param env used to check it if it is a dev environment
     * @param tokenCredentialProviderOptional Optional provider for overriding Token Credentials for connecting to App
     * Configuration.
     * @param clientProviderOptional Optional client for overriding Client Connections to App Configuration stores.
     * @param keyVaultCredentialProviderOptional optional provider, used to see if Key Vault is configured
     * @param keyVaultClientProviderOptional optional client, used to see if Key Vault is configured
     * @return ClientStore
     */
    @Bean
    @ConditionalOnMissingBean
    ClientFactory buildClientFactory(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, Environment env,
        Optional<AppConfigurationCredentialProvider> tokenCredentialProviderOptional,
        Optional<ConfigurationClientBuilderSetup> clientProviderOptional,
        Optional<KeyVaultCredentialProvider> keyVaultCredentialProviderOptional,
        Optional<SecretClientBuilderSetup> keyVaultClientProviderOptional) {

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

        boolean isDev = false;
        boolean isKeyVaultConfigured = false;

        for (String profile : env.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(profile)) {
                isDev = true;
                break;
            }
        }

        if (keyVaultCredentialProviderOptional.isPresent() || keyVaultClientProviderOptional.isPresent()) {
            isKeyVaultConfigured = true;
        }

        return new ClientFactory(properties, appProperties, tokenCredentialProvider,
            clientProvider, isDev, isKeyVaultConfigured);
    }
}
