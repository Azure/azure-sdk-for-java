// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.feature.management.entity;

import java.util.HashMap;
import java.util.List;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Azure App Configuration Feature Flag.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    @JsonProperty("key")
    private String key;

    @JsonAlias("enabled-for")
    private HashMap<Integer, FeatureFlagFilter> enabledFor;

    public Feature() {
    }

    public Feature(String key, FeatureFlagConfigurationSetting featureItem) {
        this.key = key;
        List<FeatureFlagFilter> filterMapper = featureItem.getClientFilters();

        enabledFor = new HashMap<Integer, FeatureFlagFilter>();

        for (int i = 0; i < filterMapper.size(); i++) {
            enabledFor.put(i, filterMapper.get(i));
        }
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
    public HashMap<Integer, FeatureFlagFilter> getEnabledFor() {
        return enabledFor;
    }

    /**
     * @param enabledFor the enabledFor to set
     */
    public void setEnabledFor(HashMap<Integer, FeatureFlagFilter> enabledFor) {
        this.enabledFor = enabledFor;
    }

}
