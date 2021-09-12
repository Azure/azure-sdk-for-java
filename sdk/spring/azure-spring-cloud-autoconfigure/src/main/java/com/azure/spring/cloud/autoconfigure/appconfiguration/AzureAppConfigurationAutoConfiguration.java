// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link ConfigurationClientBuilder} and Azure App Configuration clients.
 */
@ConditionalOnClass(ConfigurationClientBuilder.class)
// TODO (xiada): what's the right way to call this prefix, appconfiguration or app-configuration?
@ConditionalOnProperty(prefix = AzureAppConfigurationProperties.PREFIX, name = "enabled", matchIfMissing = true)
public class AzureAppConfigurationAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureAppConfigurationAutoConfiguration(AzureConfigurationProperties azureConfigurationProperties) {
        super(azureConfigurationProperties);
    }

    @ConfigurationProperties(prefix = AzureAppConfigurationProperties.PREFIX)
    @Bean
    public AzureAppConfigurationProperties appConfigurationProperties() {
        return copyProperties(this.azureProperties, new AzureAppConfigurationProperties());
    }

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
    public ConfigurationClientBuilder configurationClientBuilder(ConfigurationClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigurationClientBuilderFactory configurationClientBuilderFactory(AzureAppConfigurationProperties properties) {
        return new ConfigurationClientBuilderFactory(properties);
    }
}
