// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import com.azure.spring.cloud.feature.manager.DynamicFeatureManager;
import com.azure.spring.cloud.feature.manager.DynamicFeatureManagerSnapshot;
import com.azure.spring.cloud.feature.manager.FeatureHandler;
import com.azure.spring.cloud.feature.manager.FeatureManager;
import com.azure.spring.cloud.feature.manager.FeatureManagerSnapshot;
import com.azure.spring.cloud.feature.manager.IDisabledFeaturesHandler;

/**
 * Configurations setting up FeatureManagerSnapshot, FeatureHandler, FeatureConfig
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(FeatureManager.class)
public class FeatureManagementWebConfiguration {

    /**
     * Creates FeatureManagerSnapshot
     * 
     * @param featureManager App Configuration Feature Manager
     * @return FeatureManagerSnapshot
     */
    @Bean
    @RequestScope
    public FeatureManagerSnapshot featureManagerSnapshot(FeatureManager featureManager) {
        return new FeatureManagerSnapshot(featureManager);
    }

    /**
     * Creates DynamicFeatureManagerSnapshot
     * 
     * @param dynamicFeatureManager App Configuration Dynamic Feature Manager
     * @return DynamicFeatureManagerSnapshot
     */
    @Bean
    @RequestScope
    public DynamicFeatureManagerSnapshot dynamicFeatureManagerSnapshot(DynamicFeatureManager dynamicFeatureManager) {
        return new DynamicFeatureManagerSnapshot(dynamicFeatureManager);
    }

    /**
     * Creates FeatureHandler
     * 
     * @param featureManager App Configuration Feature Manager
     * @param snapshot App Configuration Feature Manager snapshot version
     * @param disabledFeaturesHandler optional handler for redirection of disabled endpoints
     * @return FeatureHandler
     */
    @Bean
    public FeatureHandler featureHandler(FeatureManager featureManager, FeatureManagerSnapshot snapshot,
        @Autowired(required = false) IDisabledFeaturesHandler disabledFeaturesHandler) {
        return new FeatureHandler(featureManager, snapshot, disabledFeaturesHandler);
    }

    /**
     * Creates FeatureConfig
     * 
     * @param featureHandler Intercepter for requests to check if then need to be blocked/redirected.
     * @return FeatureConfig
     */
    @Bean
    public FeatureConfig featureConfig(FeatureHandler featureHandler) {
        return new FeatureConfig(featureHandler);
    }

}
