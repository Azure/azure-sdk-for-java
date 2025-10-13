// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.azure.spring.cloud.feature.management.models.FeatureDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration Properties for Feature Management. Processes the configurations to be usable by Feature Management.
 */
@ConfigurationProperties(prefix = "feature-management")
public class FeatureManagementProperties {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty("feature-flags")
    private List<FeatureDefinition> featureFlags;

    //private List<Feature> convertedFeatureFlags;

    public List<FeatureDefinition> getFeatureFlags() {
        return featureFlags;
    }

    /**
     * Sets the feature flags from the configuration. This method handles conversion from various input formats.
     *
     * @param featureFlags the feature flags to set
     */
    public void setFeatureFlags(List<FeatureDefinition> featureFlags) {
        if (featureFlags == null || featureFlags.isEmpty()) {
            this.featureFlags = List.of();
            return;
        }
        
        // Check if the first element is a Feature instance to determine if we need conversion
        if (featureFlags.get(0) instanceof FeatureDefinition) {
            List<FeatureDefinition> features = new ArrayList<>();
            for (Object flag : featureFlags) {
                // This should always be true based on our check, but we verify each element to be safe
                if (flag instanceof FeatureDefinition) {
                    features.add((FeatureDefinition) flag);
                }
            }

            if (!features.isEmpty()) {
                this.featureFlags = features;
                return;
            }
        }

        // Convert the feature flags to the correct type
        List<FeatureDefinition> convertedFlags = new ArrayList<>();
        for (Object feature : featureFlags) {
            FeatureDefinition convertedFeature = OBJECT_MAPPER.convertValue(feature, FeatureDefinition.class);
            convertedFlags.add(convertedFeature);
        }
        this.featureFlags = convertedFlags;
    }

}
