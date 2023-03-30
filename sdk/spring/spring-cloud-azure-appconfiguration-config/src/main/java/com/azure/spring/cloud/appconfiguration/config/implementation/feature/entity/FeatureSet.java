// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Set of Feature Flag Key pairs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FeatureSet {

    @JsonProperty("FeatureManagement")
    private HashMap<String, Object> featureManagement;

    /**
     * Returns Map of Feature Flags.
     * 
     * @return the featureFlags
     */
    public HashMap<String, Object> getFeatureManagement() {
        return featureManagement;
    }

    /**
     * Adds a new Feature Flag.
     * 
     * @param key Name of the Feature Flag.
     * @param feature true/false, for on/off feature Flag. {@code Feature} if Feature Filter.
     */
    public void addFeature(String key, Object feature) {
        if (featureManagement == null) {
            featureManagement = new HashMap<>();
        }
        if (feature != null) {
            featureManagement.put(key, feature);
        }
    }
}
