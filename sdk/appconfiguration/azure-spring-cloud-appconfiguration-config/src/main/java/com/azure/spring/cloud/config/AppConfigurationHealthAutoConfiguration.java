// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import com.azure.spring.cloud.config.AppConfigurationAutoConfiguration.AppConfigurationWatchAutoConfiguration;
import com.azure.spring.cloud.config.health.AppConfigurationHealthIndicator;

/**
 * Health Indicator for Azure App Configuration store connections.
 */
@Configuration
@EnableAsync
@ConditionalOnClass({ HealthIndicator.class })
@ConditionalOnEnabledHealthIndicator("azure-app-configuration")
@AutoConfigureAfter(AppConfigurationWatchAutoConfiguration.class)
public class AppConfigurationHealthAutoConfiguration {

    @Bean
    @ConditionalOnBean(AppConfigurationRefresh.class)
    AppConfigurationHealthIndicator appConfigurationHealthIndicator(AppConfigurationRefresh refresh) {
        return new AppConfigurationHealthIndicator(refresh);
    }

}
