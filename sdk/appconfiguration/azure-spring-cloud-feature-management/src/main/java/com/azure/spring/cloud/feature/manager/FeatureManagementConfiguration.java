// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for setting up FeatureManager
 */
@Configuration
@EnableConfigurationProperties({FeatureManagementConfigProperties.class})
public class FeatureManagementConfiguration {

    /**
     * Creates Feature Manager
     * @param properties Feature Management configuration properties
     * @return FeatureManager
     */
    @Bean
    public FeatureManager featureManager(FeatureManagementConfigProperties properties) {
        return new FeatureManager(properties);
    }

}
