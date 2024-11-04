// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    private Map<String, Object> parameters;

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

}
