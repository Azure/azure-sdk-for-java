// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.feature.manager;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ FeatureManagementConfigProperties.class })
public class FeatureManagementConfiguration {

    @Bean
    public FeatureManager featureManager(FeatureManagementConfigProperties properties) {
        return new FeatureManager(properties);
    }

}
