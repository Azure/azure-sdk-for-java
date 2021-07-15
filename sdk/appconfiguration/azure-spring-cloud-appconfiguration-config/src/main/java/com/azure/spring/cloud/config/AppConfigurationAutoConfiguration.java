// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import com.azure.spring.cloud.config.health.AppConfigurationHealthIndicator;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.stores.ClientStore;

/**
 * Setup AppConfigurationRefresh when <i>spring.cloud.azure.appconfiguration.enabled</i> is enabled.
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = AppConfigurationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AppConfigurationAutoConfiguration {

    @Configuration
    @ConditionalOnClass(RefreshEndpoint.class)
    static class AppConfigurationWatchAutoConfiguration {

        @Bean
        public AppConfigurationRefresh getConfigWatch(AppConfigurationProperties properties, ClientStore clientStore) {
            return new AppConfigurationRefresh(properties, clientStore);
        }
    }
    
    /**
     * Health Indicator for Azure App Configuration store connections.
     */
    @Configuration
    @ConditionalOnBean(AppConfigurationRefresh.class)
    @ConditionalOnClass({ HealthIndicator.class })
    static class KeyVaultHealthConfiguration {

        @Bean
        @ConditionalOnEnabledHealthIndicator("azure-app-configuration")
        AppConfigurationHealthIndicator appConfigurationHealthIndicator(AppConfigurationRefresh refresh) {
            return new AppConfigurationHealthIndicator(refresh);
        }
    }
}
