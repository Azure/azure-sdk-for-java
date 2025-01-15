// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;

@EnableConfigurationProperties(AppConfigurationProviderProperties.class)
class AzureAppConfigurationBootstrapRegistrar {

    static void register(ConfigDataLocationResolverContext context, Binder binder,
        AppConfigurationProperties properties, AppConfigurationProviderProperties appProperties,
        ReplicaLookUp replicaLookup) {
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory = appConfigurationKeyVaultClientFactory(
            appProperties, context,
            binder);
        AppConfigurationReplicaClientsBuilder replicaClientsBuilder = replicaClientBuilder(context, binder,
            appProperties, keyVaultClientFactory);
        AppConfigurationReplicaClientFactory replicaClientFactory = buildClientFactory(replicaClientsBuilder,
            properties, replicaLookup);

        context.getBootstrapContext().registerIfAbsent(AppConfigurationKeyVaultClientFactory.class,
            InstanceSupplier.from(() -> keyVaultClientFactory));
        context.getBootstrapContext().registerIfAbsent(AppConfigurationReplicaClientFactory.class,
            InstanceSupplier.from(() -> replicaClientFactory));
    }

    private static AppConfigurationKeyVaultClientFactory appConfigurationKeyVaultClientFactory(
        AppConfigurationProviderProperties appProperties,
        ConfigDataLocationResolverContext context, Binder binder)
        throws IllegalArgumentException {

        SecretClientCustomizer customizer = context.getBootstrapContext().getOrElse(SecretClientCustomizer.class, null);
        KeyVaultSecretProvider secretProvider = context.getBootstrapContext().getOrElse(KeyVaultSecretProvider.class,
            null);
        SecretClientBuilderFactory secretClientFactory = context.getBootstrapContext()
            .getOrElse(SecretClientBuilderFactory.class, null);

        AzureGlobalProperties globalProperties = binder
            .bind(AzureGlobalProperties.PREFIX, Bindable.of(AzureGlobalProperties.class))
            .orElseGet(AzureGlobalProperties::new);
        AzureAppConfigurationProperties appConfigurationProperties = binder
            .bind(AzureAppConfigurationProperties.PREFIX, Bindable.of(AzureAppConfigurationProperties.class))
            .orElseGet(AzureAppConfigurationProperties::new);
        // the properties are used to custom the ConfigurationClientBuilder
        AzureAppConfigurationProperties properties = AzureGlobalPropertiesUtils.loadProperties(globalProperties,
            appConfigurationProperties);

        boolean isCredentialConfigured = isCredentialConfigured(properties);

        return new AppConfigurationKeyVaultClientFactory(customizer, secretProvider, secretClientFactory,
            isCredentialConfigured,
            appProperties.getMaxRetryTime());
    }

    private static AppConfigurationReplicaClientFactory buildClientFactory(
        AppConfigurationReplicaClientsBuilder clientBuilder,
        AppConfigurationProperties properties, ReplicaLookUp replicaLookup) {
        return new AppConfigurationReplicaClientFactory(clientBuilder, properties.getStores(), replicaLookup);
    }

    @SuppressWarnings("unchecked")
    private static AppConfigurationReplicaClientsBuilder replicaClientBuilder(ConfigDataLocationResolverContext context,
        Binder binder,
        AppConfigurationProviderProperties appProperties, AppConfigurationKeyVaultClientFactory keyVaultClientFactory) {

        AzureGlobalProperties globalProperties = binder
            .bind(AzureGlobalProperties.PREFIX, Bindable.of(AzureGlobalProperties.class))
            .orElseGet(AzureGlobalProperties::new);
        AzureAppConfigurationProperties appConfigurationProperties = binder
            .bind(AzureAppConfigurationProperties.PREFIX, Bindable.of(AzureAppConfigurationProperties.class))
            .orElseGet(AzureAppConfigurationProperties::new);
        // the properties are used to custom the ConfigurationClientBuilder
        AzureAppConfigurationProperties properties = AzureGlobalPropertiesUtils.loadProperties(globalProperties,
            appConfigurationProperties);
        InstanceSupplier<AzureServiceClientBuilderCustomizer<ConfigurationClientBuilder>> customizer = context
            .getBootstrapContext()
            .getRegisteredInstanceSupplier(
                (Class<AzureServiceClientBuilderCustomizer<ConfigurationClientBuilder>>) (Class<?>) AzureServiceClientBuilderCustomizer.class);
        ConfigurationClientBuilderFactory clientFactory = context.getBootstrapContext()
            .getOrElseSupply(ConfigurationClientBuilderFactory.class, () -> {
                ConfigurationClientBuilderFactory factory = new ConfigurationClientBuilderFactory(properties);
                factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG);
                if (customizer != null) {
                    factory.addBuilderCustomizer(customizer.get(context.getBootstrapContext()));
                }
                return factory;
            });
        if (customizer != null) {
            clientFactory.addBuilderCustomizer(customizer.get(context.getBootstrapContext()));
        }

        boolean isCredentialConfigured = isCredentialConfigured(properties);

        InstanceSupplier<ConfigurationClientCustomizer> configurationClientCustomizer = context
            .getBootstrapContext()
            .getRegisteredInstanceSupplier(
                (Class<ConfigurationClientCustomizer>) (Class<?>) ConfigurationClientCustomizer.class);

        ConfigurationClientCustomizer clientCustomizer = null;
        if (configurationClientCustomizer != null) {
            clientCustomizer = configurationClientCustomizer.get(context.getBootstrapContext());
        }

        return new AppConfigurationReplicaClientsBuilder(3, clientFactory, clientCustomizer, isCredentialConfigured,
            keyVaultClientFactory.isConfigured());
    }

    private static boolean isCredentialConfigured(AbstractAzureHttpConfigurationProperties properties) {
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
