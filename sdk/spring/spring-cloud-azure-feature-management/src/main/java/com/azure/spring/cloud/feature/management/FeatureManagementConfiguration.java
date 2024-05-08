// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;

/**
 * Configuration for setting up FeatureManager
 */
@Configuration
@EnableConfigurationProperties({ FeatureManagementConfigProperties.class, FeatureManagementProperties.class })
@EnableCaching
class FeatureManagementConfiguration {

    /**
     * Creates Feature Manager
     *
     * @param context ApplicationContext
     * @param featureManagementConfigurations Configuration Properties for Feature Flags
     * @param properties Feature Management configuration properties
     * @return FeatureManager
     */
    @Bean
    FeatureManager featureManager(ApplicationContext context,
        FeatureManagementProperties featureManagementConfigurations, FeatureManagementConfigProperties properties) {
        return new FeatureManager(context, featureManagementConfigurations, properties);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("settings");
    }
}
