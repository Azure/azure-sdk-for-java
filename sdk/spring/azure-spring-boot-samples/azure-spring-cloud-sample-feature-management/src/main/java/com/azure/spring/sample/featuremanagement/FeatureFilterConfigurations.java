// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.featuremanagement;

import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.PercentageFilter;
import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.TimeWindowFilter;
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

}
