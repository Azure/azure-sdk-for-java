// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Reference to a Variant containing the Variant name, configuration value, and possible status override.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariantReference {

    private String name;

    private Object configurationValue;

    private String statusOverride;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     * @return VariantReference
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
     * @return VariantReference
     */
    public VariantReference setConfigurationValue(Object configurationValue) {
        this.configurationValue = configurationValue;
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
     * @return VariantReference
     */
    public VariantReference setStatusOverride(String statusOverride) {
        this.statusOverride = statusOverride;
        return this;
    }

}
