// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;

/**
 * Configuration for setting up FeatureManager
 */
@Configuration
@EnableConfigurationProperties({ FeatureManagementConfigProperties.class, FeatureManagementProperties.class })
class FeatureManagementConfiguration {

    @Autowired
    private transient ApplicationContext appContext;

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
        FeatureManagementProperties featureManagementConfigurations, FeatureManagementConfigProperties properties,
        ObjectProvider<VariantProperties> propertiesProvider) {

        TargetingContextAccessor contextAccessor = appContext.getBeanProvider(TargetingContextAccessor.class)
            .getIfAvailable();
        TargetingEvaluationOptions evaluationOptions = appContext.getBeanProvider(TargetingEvaluationOptions.class)
            .getIfAvailable();

        if (evaluationOptions == null) {
            evaluationOptions = new TargetingEvaluationOptions();
        }

        return new FeatureManager(context, featureManagementConfigurations, properties, contextAccessor,
            evaluationOptions, propertiesProvider);
    }
}
