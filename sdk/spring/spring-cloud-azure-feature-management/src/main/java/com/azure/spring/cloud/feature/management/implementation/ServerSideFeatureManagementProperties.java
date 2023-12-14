// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation;

import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import com.azure.spring.cloud.feature.management.implementation.models.ServerSideFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "FeatureManagement")
public class ServerSideFeatureManagementProperties extends FeatureManagementProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSideFeatureManagementProperties.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

    @Override
    @SuppressWarnings("unchecked")
    protected void addToFeatures(Map<? extends String, ? extends Object> features, String key, String combined) {
        Object featureValue = features.get(key);
        if (!combined.isEmpty() && !combined.endsWith(".")) {
            combined += ".";
        }

        ServerSideFeature serverSideFeature = null;
        try {
            serverSideFeature = MAPPER.convertValue(featureValue, ServerSideFeature.class);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Found invalid feature {} with value {}.", combined + key, featureValue.toString());
        }
        // When coming from a file "feature.flag" is not a possible flag name
        if (serverSideFeature != null && serverSideFeature.getConditions() == null && serverSideFeature.getId() == null) {
            if (Map.class.isAssignableFrom(featureValue.getClass())) {
                features = (Map<String, Object>) featureValue;
                for (String fKey : features.keySet()) {
                    addToFeatures(features, fKey, combined + key);
                }
            }
        } else {
            if (serverSideFeature != null) {
                Feature feature = new Feature();
                feature.setKey(key);
                feature.setEvaluate(serverSideFeature.isEnabled());
                feature.setEnabledFor(serverSideFeature.getConditions().getClientFilters());
                feature.setRequirementType(serverSideFeature.getConditions().getRequirementType());
                featureManagement.put(key, feature);
            }
        }
    }

}
