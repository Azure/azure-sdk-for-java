// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.azure.spring.cloud.feature.manager.implementation.models.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * Configuration Properties for Feature Management. Processes the configurations to be usable by Feature Management.
 */
@ConfigurationProperties(prefix = "feature-management")
public class FeatureManagementProperties extends HashMap<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManagementProperties.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

    private static final long serialVersionUID = -1642032123104805346L;

    /**
     * Map of all Feature Flags that use Feature Filters.
     */
    private transient Map<String, Feature> featureManagement;

    /**
     * Map of all Feature Flags that are just enabled/disabled.
     */
    private Map<String, Boolean> onOff;

    public FeatureManagementProperties() {
        featureManagement = new HashMap<>();
        onOff = new HashMap<>();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m == null) {
            return;
        }

        // Need to reset or switch between on/off to conditional doesn't work
        featureManagement = new HashMap<>();
        onOff = new HashMap<>();

        Map<? extends String, ? extends Object> features = removePrefixes(m, "featureManagement");

        if (!features.isEmpty()) {
            m = features;
        }

        for (String key : m.keySet()) {
            addToFeatures(m, key, "");
        }

    }

    @SuppressWarnings("unchecked")
    private Map<? extends String, ? extends Object> removePrefixes(Map<? extends String, ? extends Object> m,
        String prefix) {
        Map<? extends String, ? extends Object> removedPrefix = new HashMap<>();
        if (m.containsKey(prefix)) {
            removedPrefix = (Map<? extends String, ? extends Object>) m.get(prefix);
        }
        return removedPrefix;
    }

    @SuppressWarnings("unchecked")
    private void addToFeatures(Map<? extends String, ? extends Object> features, String key, String combined) {
        Object featureValue = features.get(key);
        if (!combined.isEmpty() && !combined.endsWith(".")) {
            combined += ".";
        }
        if (featureValue instanceof Boolean) {
            onOff.put(combined + key, (Boolean) featureValue);
        } else {
            Feature feature = null;
            try {
                feature = MAPPER.convertValue(featureValue, Feature.class);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Found invalid feature {} with value {}.", combined + key, featureValue.toString());
            }
            // When coming from a file "feature.flag" is not a possible flag name
            if (feature != null && feature.getEnabledFor() == null && feature.getKey() == null) {
                if (Map.class.isAssignableFrom(featureValue.getClass())) {
                    features = (Map<String, Object>) featureValue;
                    for (String fKey : features.keySet()) {
                        addToFeatures(features, fKey, combined + key);
                    }
                }
            } else {
                if (feature != null) {
                    feature.setKey(key);
                    featureManagement.put(key, feature);
                }
            }
        }
    }

    /**
     * @return the featureManagement
     */
    public Map<String, Feature> getFeatureManagement() {
        return featureManagement;
    }

    /**
     * @return the onOff
     */
    public Map<String, Boolean> getOnOff() {
        return onOff;
    }

}
