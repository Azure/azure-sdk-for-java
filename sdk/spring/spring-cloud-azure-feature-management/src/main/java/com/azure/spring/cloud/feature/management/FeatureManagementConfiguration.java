// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.targeting.ContextualTargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;

/**
 * Configuration for setting up FeatureManager
 */
@Configuration
@EnableConfigurationProperties({ FeatureManagementConfigProperties.class, FeatureManagementProperties.class })
class FeatureManagementConfiguration implements ApplicationContextAware {

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
    FeatureManager featureManager(FeatureManagementProperties featureManagementConfigurations,
        FeatureManagementConfigProperties properties,
        ObjectProvider<VariantProperties> propertiesProvider,
        ObjectProvider<TargetingContextAccessor> contextAccessorProvider,
        ObjectProvider<ContextualTargetingContextAccessor> contextualAccessorProvider,
        ObjectProvider<TargetingEvaluationOptions> evaluationOptionsProvider) {

        TargetingContextAccessor contextAccessor = contextAccessorProvider.getIfAvailable();
        ContextualTargetingContextAccessor contextualTargetingContextAccessor = contextualAccessorProvider
            .getIfAvailable();
        TargetingEvaluationOptions evaluationOptions = evaluationOptionsProvider
            .getIfAvailable(() -> new TargetingEvaluationOptions());

        return new FeatureManager(appContext, featureManagementConfigurations, properties, contextAccessor,
            contextualTargetingContextAccessor, evaluationOptions, propertiesProvider);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}
