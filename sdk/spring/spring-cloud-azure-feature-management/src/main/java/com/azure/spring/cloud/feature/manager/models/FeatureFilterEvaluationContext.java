// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Context passed into Feature Filters used for evaluation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FeatureFilterEvaluationContext {

    private String name;

    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    private String featureName;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the featureName
     */
    public String getFeatureName() {
        return featureName;
    }

    /**
     * @param featureName the featureName to set
     */
    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

}
