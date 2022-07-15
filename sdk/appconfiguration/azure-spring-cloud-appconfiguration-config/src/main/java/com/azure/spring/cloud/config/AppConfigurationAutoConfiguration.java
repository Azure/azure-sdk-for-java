// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import com.azure.spring.cloud.config.implementation.AppConfigurationPullRefresh;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

/**
 * Setup AppConfigurationRefresh when <i>spring.cloud.azure.appconfiguration.enabled</i> is enabled.
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = AppConfigurationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AppConfigurationAutoConfiguration {

    /**
     * Auto Watch
     */
    @Configuration
    @ConditionalOnClass(RefreshEndpoint.class)
    public static class AppConfigurationWatchAutoConfiguration {

        @Bean
        public AppConfigurationRefresh getConfigWatch(AppConfigurationProperties properties,
                AppConfigurationProviderProperties appProperties, ClientFactory clientFactory) {
            return new AppConfigurationPullRefresh(properties, appProperties, clientFactory);
        }
    }
}
