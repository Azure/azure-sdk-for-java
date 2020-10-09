// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.PercentageFilter;
import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.TargetingFilter;
import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.TimeWindowFilter;
import com.microsoft.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.microsoft.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureFilterConfigurations {

    @Bean
    public PercentageFilter percentageFilter() {
        return new PercentageFilter();
    }

    @Bean
    public TimeWindowFilter timeWindowFilter() {
        return new TimeWindowFilter();
    }

    @Bean
    public TargetingFilter targetingFilter(ITargetingContextAccessor contextAccessor) {
        return new TargetingFilter(contextAccessor, new TargetingEvaluationOptions().setIgnoreCase(true));
    }

    @Bean
    public TargetingContextAccessor targetingContextAccessor() {
        return new TargetingContextAccessor();
    }

}
