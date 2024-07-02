// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.appconfiguration;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.spring.cloud.actuator.implementation.appconfiguration.AppConfigurationConfigHealthIndicator;
import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationAutoConfiguration;


/**
 * Configuration class of App Configuration Health
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ ConfigurationAsyncClient.class, HealthIndicator.class })
@ConditionalOnBean(ConfigurationAsyncClient.class)
@AutoConfigureAfter(AzureAppConfigurationAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-appconfiguration")
public class AppConfigurationConfigHealthConfiguration {

    @Bean
    @ConditionalOnBean(AppConfigurationRefresh.class)
    AppConfigurationConfigHealthIndicator appConfigurationHealthIndicator(AppConfigurationRefresh refresh) {
        return new AppConfigurationConfigHealthIndicator(refresh);
    }
}
