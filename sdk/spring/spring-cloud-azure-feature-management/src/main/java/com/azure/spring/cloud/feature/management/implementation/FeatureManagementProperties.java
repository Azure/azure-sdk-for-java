// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import java.util.ArrayList;
import java.util.List;

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

    //private List<Feature> convertedFeatureFlags;

    public List<Feature> getFeatureFlags() {
        return featureFlags;
    }

    /**
     * Sets the feature flags from the configuration. This method handles conversion from various input formats.
     *
     * @param featureFlags the feature flags to set
     */
    public void setFeatureFlags(List<Feature> featureFlags) {
        if (featureFlags == null || featureFlags.isEmpty()) {
            this.featureFlags = List.of();
            return;
        }
        
        // Check if the first element is a Feature instance to determine if we need conversion
        if (featureFlags.get(0) instanceof Feature) {
            List<Feature> features = new ArrayList<>();
            for (Object flag : featureFlags) {
                // This should always be true based on our check, but we verify each element to be safe
                if (flag instanceof Feature) {
                    features.add((Feature) flag);
                }
            }

            if (!features.isEmpty()) {
                this.featureFlags = features;
                return;
            }
        }

        // Convert the feature flags to the correct type
        List<Feature> convertedFlags = new ArrayList<>();
        for (Object feature : featureFlags) {
            Feature convertedFeature = OBJECT_MAPPER.convertValue(feature, Feature.class);
            convertedFlags.add(convertedFeature);
        }
        this.featureFlags = convertedFlags;
    }

}
