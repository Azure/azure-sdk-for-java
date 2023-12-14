// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import java.util.Map;

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
public class ClientSideFeatureManagementProperties extends FeatureManagementProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSideFeatureManagementProperties.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

    @SuppressWarnings("unchecked")
    @Override
    protected void addToFeatures(Map<? extends String, ? extends Object> features, String key, String combined) {
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

}
