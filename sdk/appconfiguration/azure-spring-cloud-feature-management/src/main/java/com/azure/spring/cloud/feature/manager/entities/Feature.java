// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    @JsonProperty("key")
    private String key;

    @JsonProperty("evaluate")
    private Boolean evaluate = true;

    @JsonProperty("enabled-for")
    private HashMap<Integer, FeatureFilterEvaluationContext> enabledFor;

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
     * @return the evaluate
     */
    public Boolean getEvaluate() {
        return evaluate;
    }

    /**
     * @param evaluate the evaluate to set
     */
    public void setEvaluate(Boolean evaluate) {
        this.evaluate = evaluate;
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
