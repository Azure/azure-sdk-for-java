// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import com.azure.spring.cloud.feature.manager.FeatureManager;

@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(FeatureManager.class)
public class FeatureManagementWebConfiguration {

    @Bean
    @RequestScope
    public FeatureManagerSnapshot featureManagerSnapshot(FeatureManager featureManager) {
        return new FeatureManagerSnapshot(featureManager);
    }

    @Bean
    public FeatureHandler featureHandler(FeatureManager featureManager, FeatureManagerSnapshot snapshot,
        @Autowired(required = false) IDisabledFeaturesHandler disabledFeaturesHandler) {
        return new FeatureHandler(featureManager, snapshot, disabledFeaturesHandler);
    }

    @Bean
    public FeatureConfig featureConfig(FeatureHandler featureHandler) {
        return new FeatureConfig(featureHandler);
    }

}
