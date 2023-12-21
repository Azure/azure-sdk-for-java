// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import java.util.HashMap;
import java.util.Map;

import com.azure.spring.cloud.feature.management.implementation.models.ServerSideFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.azure.spring.cloud.feature.management.implementation.models.Feature;
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

        final boolean isBackendSchema = isBackendSchema(m);
        for (String key : m.keySet()) {
            if (isBackendSchema) {
                addServerSideFeature(m, key, "");
            } else {
                addFeature(m, key, "");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addFeature(Map<? extends String, ? extends Object> features, String key, String combined) {
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
                        addFeature(features, fKey, combined + key);
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

    private void addServerSideFeature(Map<? extends String, ? extends Object> features, String key, String combined) {
        final Object featureValue = features.get(key);
        if (!combined.isEmpty() && !combined.endsWith(".")) {
            combined += ".";
        }

        ServerSideFeature serverSideFeature = null;
        try {
            serverSideFeature = MAPPER.convertValue(featureValue, ServerSideFeature.class);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Found invalid feature {} with value {}.", combined + key, featureValue.toString());
        }

        if (serverSideFeature != null && serverSideFeature.getId() == null) {
            if (Map.class.isAssignableFrom(featureValue.getClass())) {
                features = (Map<String, Object>) featureValue;
                for (String fKey : features.keySet()) {
                    addServerSideFeature(features, fKey, combined + key);
                }
            }
        } else if (serverSideFeature != null) {
            if (serverSideFeature.getConditions() != null && serverSideFeature.getConditions().getClientFilters() != null
                && serverSideFeature.getConditions().getClientFilters().size() > 0) {
                final Feature feature = new Feature();
                feature.setKey(serverSideFeature.getId());
                feature.setEvaluate(serverSideFeature.isEnabled());
                feature.setEnabledFor(serverSideFeature.getConditions().getClientFilters());
                feature.setRequirementType(serverSideFeature.getConditions().getRequirementType());
                featureManagement.put(serverSideFeature.getId(), feature);
            } else {
                onOff.put(serverSideFeature.getId(), serverSideFeature.isEnabled());
            }
        }
    }

    private boolean isBackendSchema(Map<? extends String, ? extends Object> features) {
        if (features.keySet().isEmpty()) {
            return false;
        }

        final String firstKey = features.keySet().stream().findFirst().get();
        return firstKey.equalsIgnoreCase("featureFlags");
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
