package com.azure.spring.cloud.appconfiguration.config.implementation;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.properties.bind.Binder;

import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;

class AppConfigurationBootstrapRegistrar {

    static void register(ConfigDataLocationResolverContext context, Binder binder,
        AppConfigurationProperties properties, AppConfigurationProviderProperties appProperties) {
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory = appConfigurationKeyVaultClientFactory(
            appProperties, context,
            binder);
        AppConfigurationReplicaClientsBuilder replicaClientsBuilder = replicaClientBuilder(context, binder,
            appProperties, keyVaultClientFactory);
        AppConfigurationReplicaClientFactory replicaClientFactory = buildClientFactory(replicaClientsBuilder,
            properties);

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

        return new AppConfigurationKeyVaultClientFactory(customizer, secretProvider, appProperties.getMaxRetryTime());
    }

    private static AppConfigurationReplicaClientFactory buildClientFactory(
        AppConfigurationReplicaClientsBuilder clientBuilder,
        AppConfigurationProperties properties) {
        return new AppConfigurationReplicaClientFactory(clientBuilder, properties.getStores());
    }

    private static AppConfigurationReplicaClientsBuilder replicaClientBuilder(ConfigDataLocationResolverContext context,
        Binder binder,
        AppConfigurationProviderProperties appProperties, AppConfigurationKeyVaultClientFactory keyVaultClientFactory) {

        AppConfigurationReplicaClientsBuilder clientBuilder = new AppConfigurationReplicaClientsBuilder(
            appProperties.getMaxRetries());

        InstanceSupplier<ConfigurationClientCustomizer> customizer = context.getBootstrapContext()
            .getRegisteredInstanceSupplier(ConfigurationClientCustomizer.class);
        clientBuilder.setClientProvider(customizer.get(context.getBootstrapContext()));

        clientBuilder.setIsKeyVaultConfigured(keyVaultClientFactory.isConfigured());

        return clientBuilder;
    }

}