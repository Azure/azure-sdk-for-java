// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import com.azure.spring.cloud.feature.management.implementation.models.ServerSideFeature;
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

    private static final String FEATURE_FLAG_SNAKE_CASE = "feature_flags";

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

        // try to parse the properties by server side schema as default
        tryServerSideSchema(m);

        if (featureManagement.isEmpty() && onOff.isEmpty()) {
            tryClientSideSchema(m);
        }
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private void tryServerSideSchema(Map<? extends String, ? extends Object> features) {
        if (features.keySet().isEmpty()) {
            return;
        }

        // check if FeatureFlags section exist
        String featureFlagsSectionKey = "";
        for (String key : features.keySet()) {
            if (FEATURE_FLAG_SNAKE_CASE.equalsIgnoreCase(key)) {
                featureFlagsSectionKey = key;
                break;
            }
        }
        if (featureFlagsSectionKey.isEmpty()) {
            return;
        }

        // get FeatureFlags section and parse
        final Object featureFlagsObject = features.get(featureFlagsSectionKey);
        if (Map.class.isAssignableFrom(featureFlagsObject.getClass())) {
            final Map<String, Object> featureFlagsSection = (Map<String, Object>) featureFlagsObject;
            for (String key : featureFlagsSection.keySet()) {
                addServerSideFeature(featureFlagsSection, key);
            }
        } else {
            if (List.class.isAssignableFrom(featureFlagsObject.getClass())) {
                final List<Object> featureFlagsSection = (List<Object>) featureFlagsObject;
                for (Object flag : featureFlagsSection) {
                    addServerSideFeature((Map<? extends String, ?>) flag, null);
                }
            }
        }
    }

    private void tryClientSideSchema(Map<? extends String, ? extends Object> features) {
        for (String key : features.keySet()) {
            addFeature(features, key, "");
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

    @SuppressWarnings("unchecked")
    private void addServerSideFeature(Map<? extends String, ? extends Object> features, String key) {
        Object featureValue = null;
        if (key != null) {
            featureValue = features.get(key);
        } else {
            featureValue = features;
        }

        ServerSideFeature serverSideFeature = null;
        try {
            LinkedHashMap<String, Object> ff = new LinkedHashMap<>();
            if (featureValue.getClass().isAssignableFrom(LinkedHashMap.class)) {
                ff = (LinkedHashMap<String, Object>) featureValue;
            }
            LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
            if (ff.containsKey("conditions")
                && ff.get("conditions").getClass().isAssignableFrom(LinkedHashMap.class)) {
                conditions = (LinkedHashMap<String, Object>) ff.get("conditions");
            }
            FeatureFilterUtils.updateValueFromMapToList(conditions, "client_filters");

            serverSideFeature = MAPPER.convertValue(featureValue, ServerSideFeature.class);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Found invalid feature {} with value {}.", key, featureValue.toString());
        }

        if (serverSideFeature != null && serverSideFeature.getId() != null) {
            if (serverSideFeature.getConditions() != null
                && serverSideFeature.getConditions().getClientFilters() != null
                && serverSideFeature.getConditions().getClientFilters().size() > 0) {
                final Feature feature = new Feature();
                feature.setKey(serverSideFeature.getId());
                feature.setEvaluate(serverSideFeature.isEnabled());
                feature.setEnabledFor(serverSideFeature.getConditions().getClientFiltersAsMap());
                feature.setRequirementType(serverSideFeature.getConditions().getRequirementType());
                featureManagement.put(serverSideFeature.getId(), feature);
            } else {
                onOff.put(serverSideFeature.getId(), serverSideFeature.isEnabled());
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
