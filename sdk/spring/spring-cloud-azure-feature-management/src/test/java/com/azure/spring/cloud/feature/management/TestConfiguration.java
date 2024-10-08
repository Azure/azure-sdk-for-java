// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.testobjects.DiscountBanner;

@Configuration
@ConfigurationProperties
public class TestConfiguration implements VariantProperties {

    @Bean
    public FeatureManagementConfigProperties properties() {
        return new FeatureManagementConfigProperties();
    }

    private Map<String, DiscountBanner> banner;

    /**
     * @return the banner
     */
    public Map<String, DiscountBanner> getBanner() {
        return banner;
    }

    /**
     * @param banner the banner to set
     */
    public void setBanner(Map<String, DiscountBanner> banner) {
        this.banner = banner;
    }

}
