// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.feature.manager;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Adds the feature management handler to intercept all paths.
 */
@Configuration
public class FeatureConfig implements WebMvcConfigurer {

    private final FeatureHandler featureHandler;

    public FeatureConfig (FeatureHandler featureHandler) {
        this.featureHandler = featureHandler;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureHandler)
          .addPathPatterns("/**");
    }
}
