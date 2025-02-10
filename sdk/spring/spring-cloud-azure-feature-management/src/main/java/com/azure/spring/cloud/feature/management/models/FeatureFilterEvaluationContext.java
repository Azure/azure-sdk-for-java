// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Context passed into Feature Filters used for evaluation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FeatureFilterEvaluationContext {

    /**
     * Creates an instance of {@link FeatureFilterEvaluationContext}
     */
    public FeatureFilterEvaluationContext() {
    }

    private String name;

    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    private String featureName;

    /**
     * Return the name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the parameters
     * @return the parameters
     */
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        if (parameters != null) {
            params.putAll(parameters);
        }
        return params;
    }

    /**
     * Set the parameters
     * @param parameters the parameters to set
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Return the featureName
     * @return the featureName
     */
    public String getFeatureName() {
        return featureName;
    }

    /**
     * Set the featureName
     * @param featureName the featureName to set
     */
    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

}
