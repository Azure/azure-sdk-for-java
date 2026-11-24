// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a feature flag variant.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Variant {

    @JsonProperty("name")
    private String name;

    @JsonProperty("configuration_value")
    private Object configurationValue;

    @JsonProperty("status_override")
    private String statusOverride;

    /**
     * Default constructor.
     */
    public Variant() {
    }

    /**
     * Constructor with parameters.
     * 
     * @param name the name of the variant
     * @param configurationValue the configuration value
     * @param statusOverride the status override
     */
    public Variant(String name, Object configurationValue, String statusOverride) {
        this.name = name;
        this.configurationValue = configurationValue;
        this.statusOverride = statusOverride;
    }

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
    public Object getConfigurationValue() {
        return configurationValue;
    }

    /**
     * @param configurationValue the configurationValue to set
     */
    public void setConfigurationValue(Object configurationValue) {
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
