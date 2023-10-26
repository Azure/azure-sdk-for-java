// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VariantReference {

    private String name;

    @JsonProperty("configuration-value")
    private Object configurationValue;

    @JsonProperty("configuration-reference")
    private String configurationReference;

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
    public VariantReference setName(String name) {
        this.name = name;
        return this;
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
    public VariantReference setConfigurationValue(Object configurationValue) {
        this.configurationValue = configurationValue;
        return this;
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
    public VariantReference setConfigurationReference(String configurationReference) {
        this.configurationReference = configurationReference;
        return this;
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
    public VariantReference setStatusOverride(String statusOverride) {
        this.statusOverride = statusOverride;
        return this;
    }

}
