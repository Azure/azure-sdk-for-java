package com.azure.spring.cloud.appconfiguration.config.implementation;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;

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
        AzureGlobalProperties globalSource = binder.bindOrCreate(AzureGlobalProperties.PREFIX,
            AzureGlobalProperties.class);
        AzureGlobalProperties serviceSource = binder.bindOrCreate(AzureKeyVaultSecretProperties.PREFIX,
            AzureGlobalProperties.class);

        AzureKeyVaultSecretProperties globalProperties = AzureGlobalPropertiesUtils.loadProperties(globalSource,
            new AzureKeyVaultSecretProperties());
        AzureKeyVaultSecretProperties clientProperties = AzureGlobalPropertiesUtils.loadProperties(serviceSource,
            new AzureKeyVaultSecretProperties());

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(globalProperties, clientProperties);

        SecretClientBuilderFactory secretClientBuilderFactory = new SecretClientBuilderFactory(clientProperties);

        boolean credentialConfigured = isCredentialConfigured(clientProperties);

        SecretClientCustomizer customizer = context.getBootstrapContext().getOrElse(SecretClientCustomizer.class, null);
        KeyVaultSecretProvider secretProvider = context.getBootstrapContext().getOrElse(KeyVaultSecretProvider.class, null);


        return new AppConfigurationKeyVaultClientFactory(customizer,
            secretProvider, secretClientBuilderFactory, credentialConfigured,
            appProperties.getMaxRetryTime());
    }

    private static AppConfigurationReplicaClientFactory buildClientFactory(
        AppConfigurationReplicaClientsBuilder clientBuilder,
        AppConfigurationProperties properties) {
        return new AppConfigurationReplicaClientFactory(clientBuilder, properties.getStores());
    }

    private static AppConfigurationReplicaClientsBuilder replicaClientBuilder(ConfigDataLocationResolverContext context,
        Binder binder,
        AppConfigurationProviderProperties appProperties, AppConfigurationKeyVaultClientFactory keyVaultClientFactory) {
        AzureGlobalProperties globalSource = binder.bindOrCreate(AzureGlobalProperties.PREFIX,
            AzureGlobalProperties.class);
        AzureGlobalProperties serviceSource = binder.bindOrCreate(AzureAppConfigurationProperties.PREFIX,
            AzureGlobalProperties.class);

        AzureGlobalProperties globalProperties = AzureGlobalPropertiesUtils.loadProperties(globalSource,
            new AzureGlobalProperties());
        AzureAppConfigurationProperties clientProperties = AzureGlobalPropertiesUtils.loadProperties(serviceSource,
            new AzureAppConfigurationProperties());

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(globalProperties, clientProperties);

        ConfigurationClientBuilderFactory clientFactory = new ConfigurationClientBuilderFactory(clientProperties);

        clientFactory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG);
        // TODO (mametcal) Something with custimizers in setup
        // customizers.orderedStream().forEach(clientFactory::addBuilderCustomizer);

        boolean credentialConfigured = isCredentialConfigured(clientProperties);

        AppConfigurationReplicaClientsBuilder clientBuilder = new AppConfigurationReplicaClientsBuilder(
            appProperties.getMaxRetries(), clientFactory, credentialConfigured, context);

        InstanceSupplier<ConfigurationClientCustomizer> customizer = context.getBootstrapContext()
            .getRegisteredInstanceSupplier(ConfigurationClientCustomizer.class);
        clientBuilder.setClientProvider(customizer.get(context.getBootstrapContext()));

        clientBuilder.setIsKeyVaultConfigured(keyVaultClientFactory.isConfigured());

        return clientBuilder;
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