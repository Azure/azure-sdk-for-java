// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.entities;

import java.util.LinkedHashMap;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A variant of a feature.
 */
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureVariant {

    @NotBlank
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("default")
    private Boolean isDefault = false;

    @NotBlank
    private String configurationReference;

    private LinkedHashMap<String, Object> assignmentParameters;

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
    public LinkedHashMap<String, Object> getAssignmentParameters() {
        return assignmentParameters;
    }

    /**
     * @param assignmentParameters the assignmentParameters to set
     */
    public void setAssignmentParameters(LinkedHashMap<String, Object> assignmentParameters) {
        this.assignmentParameters = assignmentParameters;
    }
}
