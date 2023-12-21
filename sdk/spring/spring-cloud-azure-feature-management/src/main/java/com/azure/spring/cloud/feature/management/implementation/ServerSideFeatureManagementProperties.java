// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation;

import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import com.azure.spring.cloud.feature.management.implementation.models.ServerSideFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "feature-management")
public class ServerSideFeatureManagementProperties extends FeatureManagementProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSideFeatureManagementProperties.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

        if (serverSideFeature != null && serverSideFeature.getId() == null) {
            if (Map.class.isAssignableFrom(featureValue.getClass())) {
                features = (Map<String, Object>) featureValue;
                for (String fKey : features.keySet()) {
                    addToFeatures(features, fKey, combined + key);
                }
            }
        } else if (serverSideFeature != null) {
            if (serverSideFeature.getConditions() != null && serverSideFeature.getConditions().getClientFilters() != null
                && serverSideFeature.getConditions().getClientFilters().size() > 0) {
                Feature feature = new Feature();
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

}
