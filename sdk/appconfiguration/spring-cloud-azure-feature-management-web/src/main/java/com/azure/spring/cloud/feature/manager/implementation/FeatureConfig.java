// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.azure.spring.cloud.feature.manager.FeatureHandler;

/**
 * Adds the feature management handler to intercept all paths.
 */
@Configuration
public class FeatureConfig implements WebMvcConfigurer {

    private final FeatureHandler featureHandler;

    /**
     * FeatureConfig enables setting up interceptors for endpoints to be managed by FeatureHandler.
     * @param featureHandler Interceptor for endpoint requests
     */
    public FeatureConfig(FeatureHandler featureHandler) {
        this.featureHandler = featureHandler;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureHandler)
            .addPathPatterns("/**");
    }
}
