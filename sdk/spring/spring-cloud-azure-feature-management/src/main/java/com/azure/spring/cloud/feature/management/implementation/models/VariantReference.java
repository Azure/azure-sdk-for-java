// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VariantReference {

    private String name;

    @JsonProperty("configuration-value")
    private Map<String, Object> configurationValue;

    private String statusOverride;

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
     * @return the configurationValue
     */
    public Map<String, Object> getConfigurationValue() {
        return configurationValue;
    }

    /**
     * @param configurationValue the configurationValue to set
     */
    public void setConfigurationValue(Map<String, Object> configurationValue) {
        this.configurationValue = configurationValue;
    }

    /**
     * @return the statusOverride
     */
    public String getStatusOverride() {
        return statusOverride;
    }

    /**
     * @param statusOverride the statusOverride to set
     */
    public void setStatusOverride(String statusOverride) {
        this.statusOverride = statusOverride;
    }

}
