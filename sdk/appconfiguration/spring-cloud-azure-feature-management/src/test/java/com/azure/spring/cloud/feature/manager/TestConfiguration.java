// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementConfigProperties;

@Configuration
@ConfigurationProperties
public class TestConfiguration {

    @Bean
    public FeatureManagementConfigProperties properties() {
        return new FeatureManagementConfigProperties();
    }
}
