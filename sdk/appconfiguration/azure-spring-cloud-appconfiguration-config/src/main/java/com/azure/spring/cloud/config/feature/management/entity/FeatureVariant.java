// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.feature.management.entity;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A variant of a feature.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureVariant {

    private String name;

    @JsonProperty("default")
    private Boolean isDefault = false;

    @JsonProperty("configuration-reference")
    @JsonAlias("configuration_reference")
    private String configurationReference;

    @JsonProperty("assignment-parameters")
    @JsonAlias("assignment_parameters")
    private Map<String, Object> assignmentParameters;

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
     * @return the default
     */
    public Boolean getDefault() {
        return isDefault;
    }

    /**
     * @param isDefault the isDefault to set
     */
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * @return the configurationReference
     */
    public String getConfigurationReference() {
        return configurationReference;
    }

    /**
     * @param configurationReference the configurationReference to set
     */
    public void setConfigurationReference(String configurationReference) {
        this.configurationReference = configurationReference;
    }

    /**
     * @return the assignmentParameters
     */
    public Map<String, Object> getAssignmentParameters() {
        return assignmentParameters;
    }

    /**
     * @param map the assignmentParameters to set
     */
    public void setAssignmentParameters(Map<String, Object> map) {
        this.assignmentParameters = map;
    }
}
