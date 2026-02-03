// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;

@Configuration
@EnableConfigurationProperties({ FeatureManagementConfigProperties.class, FeatureManagementProperties.class })
class FeatureManagementTestConfigurations {

    @Bean
    public TargetingEvaluationOptions targetingEvaluationOptions() {
        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);
        return options;
    }
}
