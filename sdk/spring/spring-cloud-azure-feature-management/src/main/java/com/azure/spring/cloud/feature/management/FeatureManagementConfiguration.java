// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.azure.spring.cloud.feature.management.filters.PercentageFilter;
import com.azure.spring.cloud.feature.management.filters.TargetingFilter;
import com.azure.spring.cloud.feature.management.filters.TimeWindowFilter;
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
    
    @Bean(name = "Microsoft.TimeWindow")
    @ConditionalOnMissingBean(TimeWindowFilter.class)
    public TimeWindowFilter timeWindowFilter() {
        return new TimeWindowFilter();
    }

    @Bean(name = "Microsoft.Percentage")
    @ConditionalOnMissingBean(PercentageFilter.class)
    public PercentageFilter percentageFilter() {
        return new PercentageFilter();
    }

    @Bean(name = "Microsoft.Targeting")
    @Scope("request")
    @ConditionalOnMissingBean(TargetingFilter.class)
    @ConditionalOnBean(TargetingContextAccessor.class)
    public TargetingFilter targettingFilter(TargetingContextAccessor context) {
        return new TargetingFilter(context, new TargetingEvaluationOptions().setIgnoreCase(true));
    }
}
