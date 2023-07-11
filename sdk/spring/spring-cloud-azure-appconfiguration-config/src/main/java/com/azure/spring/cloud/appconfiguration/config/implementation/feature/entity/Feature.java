// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_REQUIREMENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.REQUIREMENT_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Azure App Configuration Feature Flag.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Feature {

    @JsonProperty("key")
    private String key;

    @JsonProperty("enabled-for")
    private Map<Integer, FeatureFlagFilter> enabledFor;

    @JsonProperty(REQUIREMENT_TYPE)
    private String requirementType = DEFAULT_REQUIREMENT_TYPE;

    /**
     * Feature Flag object.
     */
    public Feature() {
    }

    /**
     * Feature Flag object.
     * 
     * @param key Name of the Feature Flag
     * @param featureItem Configurations of the Feature Flag.
     */
    public Feature(String key, FeatureFlagConfigurationSetting featureItem, String requirementType) {
        this.key = key;
        List<FeatureFlagFilter> filterMapper = featureItem.getClientFilters();

        enabledFor = new HashMap<>();

        for (int i = 0; i < filterMapper.size(); i++) {
            enabledFor.put(i, filterMapper.get(i));
        }
        this.requirementType = requirementType;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the enabledFor
     */
    public Map<Integer, FeatureFlagFilter> getEnabledFor() {
        return enabledFor;
    }

    /**
     * @param enabledFor the enabledFor to set
     */
    public void setEnabledFor(Map<Integer, FeatureFlagFilter> enabledFor) {
        this.enabledFor = enabledFor;
    }

    /**
     * @return the requirementType
     */
    public String getRequirementType() {
        return requirementType;
    }

    /**
     * @param requirementType the requirementType to set
     */
    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

}
