// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.actuator.autoconfigure.impl.appconfiguration;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.config.AppConfigurationAutoConfiguration.AppConfigurationWatchAutoConfiguration;
import com.azure.spring.cloud.actuator.appconfiguration.AppConfigurationConfigHealthIndicator;
import com.azure.spring.cloud.config.AppConfigurationRefresh;

/**
 * Health Indicator for Azure App Configuration store connections.
 */
@Configuration
@ConditionalOnClass({ HealthIndicator.class })
@ConditionalOnEnabledHealthIndicator("azure-app-configuration")
@AutoConfigureAfter(AppConfigurationWatchAutoConfiguration.class)
public class AppConfigurationConfigHealthConfiguration {

    @Bean
    @ConditionalOnBean(AppConfigurationRefresh.class)
    AppConfigurationConfigHealthIndicator appConfigurationHealthIndicator(AppConfigurationRefresh refresh) {
        return new AppConfigurationConfigHealthIndicator(refresh);
    }

}
