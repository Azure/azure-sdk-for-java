package com.azure.spring.cloud.feature.manager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.manager.entities.DynamicFeature;
import com.azure.spring.cloud.feature.manager.entities.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Text
 */
@ConfigurationProperties(prefix = "feature-management")
public class FeatureManagementProperties extends HashMap<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManagementProperties.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    

    private static final long serialVersionUID = -1642032123104805346L;

    /**
     * Map of all Feature Flags that use Feature Filters.
     */
    private transient Map<String, Feature> featureManagement;

    /**
     * Map of all Feature Flags that are just enabled/disabled.
     */
    private Map<String, Boolean> onOff;

    private transient Map<String, DynamicFeature> dynamicFeatures;
    
    public FeatureManagementProperties() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(DynamicFeature.class, new DynamicFeatureDeserializer());
        MAPPER.registerModule(module);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m == null) {
            return;
        }

        // Need to reset or switch between on/off to conditional doesn't work
        featureManagement = new HashMap<>();
        onOff = new HashMap<>();
        dynamicFeatures = new HashMap<>();

        Map<? extends String, ? extends Object> features = removePrefixes(m, "featureManagement");

        if (!features.isEmpty()) {
            m = features;
        }

        Map<? extends String, ? extends Object> featureFlags = removePrefixes(m, "feature-flags");
        Map<? extends String, ? extends Object> dynamicFeatures = removePrefixes(m, "dynamic-features");

        if (featureFlags.size() > 0 || dynamicFeatures.size() > 0) {
            for (String key : featureFlags.keySet()) {
                addToFeatures(featureFlags, key, "");
            }

            for (String key : dynamicFeatures.keySet()) {
                addToFeatures(dynamicFeatures, key, "");
            }
        } else {
            for (String key : m.keySet()) {
                addToFeatures(m, key, "");
            }
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
        Object featureKey = features.get(key);
        if (!combined.isEmpty() && !combined.endsWith(".")) {
            combined += ".";
        }
        if (featureKey instanceof Boolean) {
            onOff.put(combined + key, (Boolean) featureKey);
        } else {
            Feature feature = null;
            DynamicFeature dynamicFeature = null;
            try {
                feature = MAPPER.convertValue(featureKey, Feature.class);
                dynamicFeature = MAPPER.convertValue(featureKey, DynamicFeature.class);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Found invalid feature {} with value {}.", combined + key, featureKey.toString());
            }
            // When coming from a file "feature.flag" is not a possible flag name
            if (dynamicFeature != null && StringUtils.hasText(dynamicFeature.getAssigner())
                && dynamicFeature.getVariants().size() > 0) {
                dynamicFeatures.put(key, dynamicFeature);
            } else if (feature != null && feature.getEnabledFor() == null && feature.getKey() == null) {
                if (LinkedHashMap.class.isAssignableFrom(featureKey.getClass())) {
                    features = (LinkedHashMap<String, Object>) featureKey;
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

    /**
     * @return the dynamicFeatures
     */
    public Map<String, DynamicFeature> getDynamicFeatures() {
        return dynamicFeatures;
    }

}
