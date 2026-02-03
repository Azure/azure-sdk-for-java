// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Reference to a Variant containing the Variant name, configuration value, and possible status override.
 * This class provides a way to reference variants in feature flag configurations and their associated values.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariantReference {

    /**
     * The name of the variant reference.
     */
    private String name;

    /**
     * The configuration value associated with this variant reference.
     * This can be any type of object depending on the feature configuration.
     */
    @JsonProperty("configuration_value")
    private Object configurationValue;

    /**
     * The status override that can be used to override the default status of a feature flag.
     */
    private String statusOverride;
    
    /**
     * Creates a new instance of the VariantReference class.
     */
    public VariantReference() {
    }
    
    /**
     * Gets the name of this variant reference.
     *
     * @return the name of the variant reference
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this variant reference.
     *
     * @param name the name to set for this variant reference
     * @return the updated VariantReference instance for method chaining
     */
    public VariantReference setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the configuration value associated with this variant reference.
     *
     * @return the configuration value of this variant reference
     */
    public Object getConfigurationValue() {
        return configurationValue;
    }

    /**
     * Sets the configuration value for this variant reference.
     *
     * @param configurationValue the configuration value to set for this variant reference
     * @return the updated VariantReference instance for method chaining
     */
    public VariantReference setConfigurationValue(Object configurationValue) {
        this.configurationValue = configurationValue;
        return this;
    }
    
    /**
     * Gets the status override associated with this variant reference.
     *
     * @return the status override of this variant reference
     */
    public String getStatusOverride() {
        return statusOverride;
    }

    /**
     * Sets the status override for this variant reference.
     *
     * @param statusOverride the status override to set for this variant reference
     * @return the updated VariantReference instance for method chaining
     */
    public VariantReference setStatusOverride(String statusOverride) {
        this.statusOverride = statusOverride;
        return this;
    }

}
