// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.config.AppConfigurationRefresh;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.web.pullrefresh.AppConfigurationEventListener;
import com.azure.spring.cloud.config.web.pushbusrefresh.AppConfigurationBusRefreshEndpoint;
import com.azure.spring.cloud.config.web.pushbusrefresh.AppConfigurationBusRefreshEventListener;
import com.azure.spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEndpoint;
import com.azure.spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEventListener;

/**
 * Sets up refresh methods based on dependencies.
 */
@Configuration
@EnableConfigurationProperties(AppConfigurationProperties.class)
@RemoteApplicationEventScan
@ConditionalOnBean(AppConfigurationRefresh.class)
public class AppConfigurationWebAutoConfiguration {

    // Refresh from appconfiguration-refresh
    @Bean
    @ConditionalOnClass(RefreshEndpoint.class)
    public AppConfigurationEventListener configListener(AppConfigurationRefresh appConfigurationRefresh) {
        return new AppConfigurationEventListener(appConfigurationRefresh);
    }

    /**
     * Refresh from Pull Requests
     */

    @Configuration
    @ConditionalOnClass(name = {
        "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties",
        "org.springframework.cloud.endpoint.RefreshEndpoint"
    })
    public static class AppConfigurationPushRefreshConfiguration {

        @Bean
        public AppConfigurationRefreshEndpoint appConfigurationRefreshEndpoint(ContextRefresher contextRefresher,
            AppConfigurationProperties appConfiguration) {
            return new AppConfigurationRefreshEndpoint(contextRefresher, appConfiguration);
        }

        @Bean
        public AppConfigurationRefreshEventListener appConfigurationRefreshEventListener(
            AppConfigurationRefresh appConfigurationRefresh) {
            return new AppConfigurationRefreshEventListener(appConfigurationRefresh);
        }
    }

    /**
     * Refresh from appconfiguration-refresh-bus
     */
    @Configuration
    @ConditionalOnClass(name = {
        "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties",
        "org.springframework.cloud.bus.BusProperties",
        "org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent",
        "org.springframework.cloud.endpoint.RefreshEndpoint" })
    public static class AppConfigurationBusConfiguration {

        @Bean
        public AppConfigurationBusRefreshEndpoint appConfigurationBusRefreshEndpoint(ApplicationContext context,
            BusProperties bus, AppConfigurationProperties appConfiguration, Destination.Factory destinationFactory) {
            return new AppConfigurationBusRefreshEndpoint(context, bus.getId(), destinationFactory, appConfiguration);
        }

        @Bean
        public AppConfigurationBusRefreshEventListener appConfigurationBusRefreshEventListener(
            AppConfigurationRefresh appConfigurationRefresh) {
            return new AppConfigurationBusRefreshEventListener(appConfigurationRefresh);
        }
    }

}
