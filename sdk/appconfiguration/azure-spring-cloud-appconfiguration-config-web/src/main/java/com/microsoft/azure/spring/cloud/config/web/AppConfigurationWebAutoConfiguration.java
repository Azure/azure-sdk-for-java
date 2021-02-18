// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.spring.cloud.config.AppConfigurationRefresh;

@Configuration
@ConditionalOnBean(AppConfigurationRefresh.class)
public class AppConfigurationWebAutoConfiguration {

    @Configuration
    @ConditionalOnClass(RefreshEndpoint.class)
    static class AppConfigurationWatchAutoConfiguration {

        @Bean
        public ConfigListener configListener(AppConfigurationRefresh appConfigurationRefresh) {
            return new ConfigListener(appConfigurationRefresh);
        }
    }
}
