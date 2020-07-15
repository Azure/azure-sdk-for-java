/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;

@Configuration
@EnableAsync
public class AppConfigurationAutoConfiguration {

    @Configuration
    @ConditionalOnClass(RefreshEndpoint.class)
    static class AppConfigurationWatchAutoConfiguration {

        @Bean
        public AppConfigurationRefresh getConfigWatch(AppConfigurationProperties properties, ClientStore clientStore) {
            return new AppConfigurationRefresh(properties, clientStore);
        }
    }
}
