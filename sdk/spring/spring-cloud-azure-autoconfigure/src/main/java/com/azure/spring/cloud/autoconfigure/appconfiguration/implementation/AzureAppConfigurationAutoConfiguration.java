// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.appconfiguration.implementation;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationProperty;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Autoconfiguration for a {@link ConfigurationClientBuilder} and Azure App Configuration clients.
 */
@ConditionalOnClass(ConfigurationClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.appconfiguration.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.appconfiguration", name = {"endpoint", "connection-string"})
public class AzureAppConfigurationAutoConfiguration {

    // TODO might be better to allow config.get("connection-string") in azure-core
    private static final ConfigurationProperty<String> CONNECTION_STRING_PROPERTY = ConfigurationProperty.stringPropertyBuilder("connection-string").build();

    @Bean
    @ConditionalOnMissingBean
    public ConfigurationClient azureConfigurationClient(ConfigurationClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigurationAsyncClient azureConfigurationAsyncClient(ConfigurationClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    ConfigurationClientBuilder configurationClientBuilder(ConfigurationBuilder sdkConfigurationBuilder, DefaultAzureCredential defaultCredential) {
        Configuration appconfigSection = sdkConfigurationBuilder.section("appconfiguration").build();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        if (!appconfigSection.contains(CONNECTION_STRING_PROPERTY)) {
            builder.credential(defaultCredential);
        }

        return builder.configuration(appconfigSection);
    }

    // TODO: how is it used?
    @Bean
    @ConditionalOnProperty("spring.cloud.azure.appconfiguration.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.AppConfiguration> staticAppConfigurationConnectionStringProvider(ConfigurationBuilder sdkConfigurationBuilder) {
        return new StaticConnectionStringProvider<>(AzureServiceType.APP_CONFIGURATION,
            sdkConfigurationBuilder.section("appconfiguration").build()
                .get(CONNECTION_STRING_PROPERTY));
    }
}
