// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.feature.manager.entities;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureFilterEvaluationContext {

    @JsonProperty("name")
    private String name;

    @JsonProperty("parameters")
    private LinkedHashMap<String, Object> parameters;

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
    public LinkedHashMap<String, Object> getParameters() {
        return parameters;
    }
    /**
     * @param parameters the parameters to set
     */
    public void setParameters(LinkedHashMap<String, Object> parameters) {
        this.parameters = parameters;
    }



}
