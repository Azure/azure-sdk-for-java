// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VariantReference {

    private String name;

    @JsonProperty("configuration-value")
    private String configurationValue;

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
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the configurationValue
     */
    public String getConfigurationValue() {
        return configurationValue;
    }

    /**
     * @param configurationValue the configurationValue to set
     */
    public void setConfigurationValue(String configurationValue) {
        this.configurationValue = configurationValue;
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
