// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure App Configuration support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(ConfigurationClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.appconfiguration.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.appconfiguration", name = {"endpoint", "connection-string"})
public class AzureAppConfigurationAutoConfiguration extends AzureServiceConfigurationBase {

    AzureAppConfigurationAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @ConfigurationProperties(prefix = AzureAppConfigurationProperties.PREFIX)
    @Bean
    AzureAppConfigurationProperties azureAppConfigurationProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureAppConfigurationProperties());
    }

    /**
     * Autoconfigure the {@link ConfigurationClient} instance.
     * @param builder The {@link ConfigurationClientBuilder} to build the instance.
     * @return the configuration client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigurationClient azureConfigurationClient(ConfigurationClientBuilder builder) {
        return builder.buildClient();
    }

    /**
     * Autoconfigure the {@link ConfigurationAsyncClient} instance.
     * @param builder The {@link ConfigurationClientBuilder} to build the instance.
     * @return the configuration async client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigurationAsyncClient azureConfigurationAsyncClient(ConfigurationClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    ConfigurationClientBuilder configurationClientBuilder(ConfigurationClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    ConfigurationClientBuilderFactory configurationClientBuilderFactory(
        AzureAppConfigurationProperties properties,
        ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.AppConfiguration>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ConfigurationClientBuilder>> customizers) {
        ConfigurationClientBuilderFactory factory = new ConfigurationClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG);
        connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.appconfiguration.connection-string")
    StaticConnectionStringProvider<AzureServiceType.AppConfiguration> staticAppConfigurationConnectionStringProvider(
        AzureAppConfigurationProperties properties) {

        return new StaticConnectionStringProvider<>(AzureServiceType.APP_CONFIGURATION, properties.getConnectionString());
    }
}
