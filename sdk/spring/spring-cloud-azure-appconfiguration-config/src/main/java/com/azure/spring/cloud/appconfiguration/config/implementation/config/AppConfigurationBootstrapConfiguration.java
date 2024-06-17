// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.config;

import javax.naming.NamingException;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationKeyVaultClientFactory;
import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationPropertySourceLocator;
import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientFactory;
import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientsBuilder;
import com.azure.spring.cloud.appconfiguration.config.implementation.FeatureFlagClient;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;

/**
 * Setup ConnectionPool, AppConfigurationPropertySourceLocator, and ClientStore when
 * <i>spring.cloud.azure.appconfiguration.enabled</i> is enabled.
 */
@Configuration
@PropertySource("classpath:appConfiguration.properties")
@EnableConfigurationProperties({ AppConfigurationProperties.class, AppConfigurationProviderProperties.class })
@ConditionalOnClass(AppConfigurationPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AppConfigurationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
@EnableAsync
public class AppConfigurationBootstrapConfiguration {

    @Autowired
    private transient ApplicationContext context;

    @Bean
    AppConfigurationPropertySourceLocator sourceLocator(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, AppConfigurationReplicaClientFactory clientFactory,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, ReplicaLookUp replicaLookUp,
        FeatureFlagClient featureFlagLoader)
        throws IllegalArgumentException {

        return new AppConfigurationPropertySourceLocator(appProperties, clientFactory, keyVaultClientFactory,
            properties.getRefreshInterval(), properties.getStores(), replicaLookUp, featureFlagLoader);
    }

    @Bean
    AppConfigurationKeyVaultClientFactory appConfigurationKeyVaultClientFactory(Environment environment,
        AppConfigurationProviderProperties appProperties)
        throws IllegalArgumentException {
        AzureGlobalProperties globalSource = Binder.get(environment).bindOrCreate(AzureGlobalProperties.PREFIX,
            AzureGlobalProperties.class);
        AzureGlobalProperties serviceSource = Binder.get(environment).bindOrCreate(AzureKeyVaultSecretProperties.PREFIX,
            AzureGlobalProperties.class);

        AzureKeyVaultSecretProperties globalProperties = AzureGlobalPropertiesUtils.loadProperties(
            globalSource,
            new AzureKeyVaultSecretProperties());
        AzureKeyVaultSecretProperties clientProperties = AzureGlobalPropertiesUtils.loadProperties(serviceSource,
            new AzureKeyVaultSecretProperties());

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(globalProperties, clientProperties);

        SecretClientCustomizer keyVaultClientProvider = context.getBeanProvider(SecretClientCustomizer.class)
            .getIfAvailable();
        KeyVaultSecretProvider keyVaultSecretProvider = context.getBeanProvider(KeyVaultSecretProvider.class)
            .getIfAvailable();

        SecretClientBuilderFactory secretClientBuilderFactory = new SecretClientBuilderFactory(clientProperties);

        boolean credentialConfigured = isCredentialConfigured(clientProperties);

        return new AppConfigurationKeyVaultClientFactory(keyVaultClientProvider, keyVaultSecretProvider,
            secretClientBuilderFactory, credentialConfigured, appProperties.getMaxRetryTime());
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
    AppConfigurationReplicaClientFactory buildClientFactory(AppConfigurationReplicaClientsBuilder clientBuilder,
        AppConfigurationProperties properties, ReplicaLookUp replicaLookUp) {
        return new AppConfigurationReplicaClientFactory(clientBuilder, properties.getStores(), replicaLookUp);
    }

    /**
     * Loader for all feature flags. Enables de-duplicating of feature flags when multiple feature flags with the same
     * name are loaded.
     * @return {@link FeatureFlagClient}
     */
    @Bean
    @ConditionalOnMissingBean
    FeatureFlagClient featureFlagLoader() {
        return new FeatureFlagClient();
    }

    /**
     * Builder for clients connecting to App Configuration.
     *
     * @param clientProperties AzureAppConfigurationProperties Spring Cloud Azure global properties.
     * @param appProperties Library configurations for setting up connections to each config store.
     * @param keyVaultClientFactory used for tracing info for if key vault has been configured
     * @param customizers Client Customizers for connecting to Azure App Configuration
     * @return ClientStore
     */
    @Bean
    @ConditionalOnMissingBean
    AppConfigurationReplicaClientsBuilder replicaClientBuilder(Environment environment,
        AppConfigurationProviderProperties appProperties, AppConfigurationKeyVaultClientFactory keyVaultClientFactory,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ConfigurationClientBuilder>> customizers) {
        AzureGlobalProperties globalSource = Binder.get(environment).bindOrCreate(AzureGlobalProperties.PREFIX,
            AzureGlobalProperties.class);
        AzureGlobalProperties serviceSource = Binder.get(environment).bindOrCreate(
            AzureAppConfigurationProperties.PREFIX,
            AzureGlobalProperties.class);

        AzureGlobalProperties globalProperties = AzureGlobalPropertiesUtils.loadProperties(globalSource,
            new AzureGlobalProperties());
        AzureAppConfigurationProperties clientProperties = AzureGlobalPropertiesUtils.loadProperties(serviceSource,
            new AzureAppConfigurationProperties());

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(globalProperties, clientProperties);

        ConfigurationClientBuilderFactory clientFactory = new ConfigurationClientBuilderFactory(clientProperties);

        clientFactory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG);
        customizers.orderedStream().forEach(clientFactory::addBuilderCustomizer);

        boolean credentialConfigured = isCredentialConfigured(clientProperties);

        AppConfigurationReplicaClientsBuilder clientBuilder = new AppConfigurationReplicaClientsBuilder(
            appProperties.getMaxRetries(), clientFactory, credentialConfigured);

        clientBuilder
            .setClientProvider(context.getBeanProvider(ConfigurationClientCustomizer.class)
                .getIfAvailable());

        clientBuilder.setIsKeyVaultConfigured(keyVaultClientFactory.isConfigured());

        return clientBuilder;
    }

    @Bean
    ReplicaLookUp replicaLookUp(AppConfigurationProperties properties) throws NamingException {
        return new ReplicaLookUp(properties);
    }

    private boolean isCredentialConfigured(AbstractAzureHttpConfigurationProperties properties) {
        if (properties.getCredential() != null) {
            TokenCredentialConfigurationProperties tokenProps = properties.getCredential();
            if (StringUtils.hasText(tokenProps.getClientCertificatePassword())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getClientCertificatePath())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getClientId())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getClientSecret())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getUsername())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getPassword())) {
                return true;
            }
        }

        return false;
    }

}
