// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import com.azure.spring.cloud.feature.management.implementation.ClientSideFeatureManagementProperties;
import com.azure.spring.cloud.feature.management.implementation.ServerSideFeatureManagementProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;

/**
 * Configuration for setting up FeatureManager
 */
@Configuration
@EnableConfigurationProperties({ FeatureManagementConfigProperties.class, ClientSideFeatureManagementProperties.class, ServerSideFeatureManagementProperties.class })
class FeatureManagementConfiguration {

    /**
     * Creates Feature Manager
     *
     * @param context ApplicationContext
     * @param clientSideConfigurations Configuration Properties for Feature Flags
     * @param properties Feature Management configuration properties
     * @return FeatureManager
     */
    @Bean
    FeatureManager featureManager(ApplicationContext context,
                                  ClientSideFeatureManagementProperties clientSideConfigurations,
                                  ServerSideFeatureManagementProperties serverSideConfigurations,
                                  FeatureManagementConfigProperties properties) {
        return new FeatureManager(context, clientSideConfigurations, serverSideConfigurations, properties);
    }
}
