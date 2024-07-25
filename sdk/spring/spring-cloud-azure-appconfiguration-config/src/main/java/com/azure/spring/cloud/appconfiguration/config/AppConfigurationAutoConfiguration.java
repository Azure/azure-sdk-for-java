// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationPullRefresh;
import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientFactory;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;

/**
 * Setup AppConfigurationRefresh when <i>spring.cloud.azure.appconfiguration.enabled</i> is enabled.
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = AppConfigurationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AppConfigurationAutoConfiguration {

    /**
     * Creates an instance of {@link AppConfigurationAutoConfiguration}
     */
    public AppConfigurationAutoConfiguration() {
    }

    /**
     * Auto Watch
     */
    @Configuration
    @ConditionalOnClass(RefreshEndpoint.class)
    public static class AppConfigurationWatchAutoConfiguration {

        /**
         * Creates an instance of {@link AppConfigurationWatchAutoConfiguration}
         */
        public AppConfigurationWatchAutoConfiguration() {
        }

        @Bean
        @ConditionalOnMissingBean
        AppConfigurationRefresh appConfigurationRefresh(AppConfigurationProperties properties,
            AppConfigurationProviderProperties appProperties, AppConfigurationReplicaClientFactory clientFactory, ReplicaLookUp replicaLookUp) {
            return new AppConfigurationPullRefresh(clientFactory, properties.getRefreshInterval(),
                appProperties.getDefaultMinBackoff(), replicaLookUp);
        }
    }
}
