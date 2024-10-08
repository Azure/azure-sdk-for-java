// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
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
        
        boolean isCredentialConfigured = isCredentialConfigured(null);

        return new AppConfigurationKeyVaultClientFactory(customizer, secretProvider, secretClientFactory, isCredentialConfigured,
            appProperties.getMaxRetryTime());
    }

    private static AppConfigurationReplicaClientFactory buildClientFactory(
        AppConfigurationReplicaClientsBuilder clientBuilder,
        AppConfigurationProperties properties, ReplicaLookUp replicaLookup) {
        return new AppConfigurationReplicaClientFactory(clientBuilder, properties.getStores(), replicaLookup);
    }

    private static AppConfigurationReplicaClientsBuilder replicaClientBuilder(ConfigDataLocationResolverContext context,
        Binder binder,
        AppConfigurationProviderProperties appProperties, AppConfigurationKeyVaultClientFactory keyVaultClientFactory) {

        ConfigurationClientBuilderFactory clientFactory = context.getBootstrapContext()
            .getOrElse(ConfigurationClientBuilderFactory.class, null);

        boolean isCredentialConfigured = isCredentialConfigured(null);

        AppConfigurationReplicaClientsBuilder clientBuilder = new AppConfigurationReplicaClientsBuilder(
            appProperties.getMaxRetries(), clientFactory,  isCredentialConfigured);

        InstanceSupplier<ConfigurationClientCustomizer> customizer = context.getBootstrapContext()
            .getRegisteredInstanceSupplier(ConfigurationClientCustomizer.class);
        if (customizer != null) {
            clientBuilder.setClientProvider(customizer.get(context.getBootstrapContext()));
        }

        clientBuilder.setIsKeyVaultConfigured(keyVaultClientFactory.isConfigured());

        return clientBuilder;
    }

    private static boolean isCredentialConfigured(AbstractAzureHttpConfigurationProperties properties) {
        // TODO (mametcal) Temp till global properties is setup.
        if (properties == null) {
            return false;
        }

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
