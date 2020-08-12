/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.TargetingFilter;
import com.microsoft.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.microsoft.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;

@Configuration
public class FeatureFilterConfigurations {
    
    @Bean
    public TargetingFilter targetingFilter(ITargetingContextAccessor contextAccessor) {
        return new TargetingFilter(contextAccessor, new TargetingEvaluationOptions().setIgnoreCase(true));
    }

    @Bean
    @RequestScope
    public TargetingContextAccessor targetingContextAccessor() {
        return new TargetingContextAccessor();
    }

}
