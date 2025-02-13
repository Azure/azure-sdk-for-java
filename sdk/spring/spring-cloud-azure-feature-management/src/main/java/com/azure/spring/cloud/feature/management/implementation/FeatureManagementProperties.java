// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.azure.spring.cloud.feature.management.models.Feature;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration Properties for Feature Management. Processes the configurations to be usable by Feature Management.
 */
@ConfigurationProperties(prefix = "feature-management")
public class FeatureManagementProperties {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty("feature-flags")
    private List<Feature> featureFlags;

    public List<Feature> getFeatureFlags() {
        return featureFlags;
    }

    public void setFeatureFlags(List<Map<String, Object>> featureFlags) {
        this.featureFlags = new ArrayList<>();
        for (Map<String, Object> featureFlag: featureFlags) {
            this.featureFlags.add(OBJECT_MAPPER.convertValue(featureFlag, Feature.class));
        }
    }

}
