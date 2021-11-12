// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.spring.cloud.actuate.appconfiguration.AppConfigurationHealthIndicator;
import com.azure.spring.cloud.autoconfigure.appconfiguration.AzureAppConfigurationAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class of App Configuration Health
 */
@Configuration
@ConditionalOnClass({ ConfigurationClient.class, HealthIndicator.class })
@ConditionalOnBean(ConfigurationClient.class)
@AutoConfigureAfter(AzureAppConfigurationAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-app-configuration")
@ConditionalOnProperty(value = "spring.cloud.azure.appconfiguration.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.appconfiguration", name = {"endpoint", "connection-string"})
public class AppConfigurationHealthConfiguration {

    @Bean
    AppConfigurationHealthIndicator appConfigurationHealthIndicator(ConfigurationClient configurationClient) {
        return new AppConfigurationHealthIndicator(configurationClient);
    }
}
