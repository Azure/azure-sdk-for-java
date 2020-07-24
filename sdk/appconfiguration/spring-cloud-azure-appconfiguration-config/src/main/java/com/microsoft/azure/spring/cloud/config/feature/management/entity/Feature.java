// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.feature.management.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    @JsonProperty("key")
    private String key;

    @JsonAlias("enabled-for")
    private HashMap<Integer, FeatureFilterEvaluationContext> enabledFor;

    public Feature() {
    }

    public Feature(String key, FeatureManagementItem featureItem) {
        this.key = key;
        List<FeatureFilterEvaluationContext> filterMapper = featureItem.getConditions().getClientFilters();

        enabledFor = new HashMap<Integer, FeatureFilterEvaluationContext>();

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
    public HashMap<Integer, FeatureFilterEvaluationContext> getEnabledFor() {
        return enabledFor;
    }

    /**
     * @param enabledFor the enabledFor to set
     */
    public void setEnabledFor(HashMap<Integer, FeatureFilterEvaluationContext> enabledFor) {
        this.enabledFor = enabledFor;
    }

}
