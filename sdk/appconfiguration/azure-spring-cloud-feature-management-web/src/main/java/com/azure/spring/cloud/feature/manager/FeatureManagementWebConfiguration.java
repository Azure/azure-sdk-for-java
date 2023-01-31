// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Configurations setting up FeatureManagerSnapshot, FeatureHandler, FeatureConfig
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(FeatureManager.class)
public class FeatureManagementWebConfiguration {

    /**
     * Creates FeatureManagerSnapshot
     * @param featureManager App Configuration Feature Manager
     * @return FeatureManagerSnapshot
     */
    @Bean
    @RequestScope
    FeatureManagerSnapshot featureManagerSnapshot(FeatureManager featureManager) {
        return new FeatureManagerSnapshot(featureManager);
    }

    /**
     * Creates FeatureHandler
     * @param featureManager App Configuration Feature Manager
     * @param snapshot App Configuration Feature Manager snapshot version
     * @param disabledFeaturesHandlerProvider optional handler for redirection of disabled endpoints
     * @return FeatureHandler
     */
    @Bean
    FeatureHandler featureHandler(FeatureManager featureManager, FeatureManagerSnapshot snapshot,
                                  ObjectProvider<IDisabledFeaturesHandler> disabledFeaturesHandlerProvider) {
        return new FeatureHandler(featureManager, snapshot, disabledFeaturesHandlerProvider.getIfAvailable());
    }

    /**
     * Creates FeatureConfig
     * @param featureHandler  Interceptor for requests to check if then need to be blocked/redirected.
     * @return FeatureConfig
     */
    @Bean
    FeatureConfig featureConfig(FeatureHandler featureHandler) {
        return new FeatureConfig(featureHandler);
    }

}
