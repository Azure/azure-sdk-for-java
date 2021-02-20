/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.PercentageFilter;
import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.TimeWindowFilter;

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

}
