// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * This class can be used in FunctionDefinition.
 *
 * @see FunctionProperties
 * @see FunctionDefinition
 */
public class FunctionParameters {
    /* Type of Parameter */
    private String type;

    /* Properties of the parameter */
    private Map<String, FunctionProperties> properties;

    /* Required properties */
    @JsonProperty(value = "required")
    private List<String> requiredPropertyNames;

    /**
     * Get type of parameter.
     *
     * @return Type of parameter.
     */
    public String getType() {
        return type;
    }

    /**
     * Set type of parameter.
     *
     * @param type Type of parameter.
     * @return Object itself.
     */
    public FunctionParameters setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get properties of parameter.
     *
     * @return All properties for parameter.
     */
    public Map<String, FunctionProperties> getProperties() {
        return properties;
    }

    /**
     * Set properties of parameter.
     *
     * @param properties Properties of parameter.
     * @return Object itself.
     */
    public FunctionParameters setProperties(Map<String, FunctionProperties> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get required properties.
     *
     * @return List of required properties.
     */
    public List<String> getRequiredPropertyNames() {
        return requiredPropertyNames;
    }

    /**
     * Set required properties.
     *
     * @param requiredPropertyNames List of required properties.
     * @return Object itself.
     */
    public FunctionParameters setRequiredPropertyNames(List<String> requiredPropertyNames) {
        this.requiredPropertyNames = requiredPropertyNames;
        return this;
    }
}
