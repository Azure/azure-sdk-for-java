// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.azure.spring.cloud.feature.management.models.Feature;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration Properties for Feature Management. Processes the configurations to be usable by Feature Management.
 */
@ConfigurationProperties(prefix = "feature-management")
public class FeatureManagementProperties {

    @JsonProperty("feature-flags")
    private List<Feature> featureFlags;

    public List<Feature> getFeatureFlags() {
        return featureFlags;
    }

    public void setFeatureFlags(List<Feature> featureFlags) {
        this.featureFlags = featureFlags;
    }

}
