/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a virtual machine scale set network profile's IP configuration.
 */
public class VirtualMachineScaleSetIPConfiguration extends SubResource {
    /**
     * Gets or sets the IP configuration name.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * The properties property.
     */
    private VirtualMachineScaleSetIPConfigurationProperties properties;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public VirtualMachineScaleSetIPConfigurationProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(VirtualMachineScaleSetIPConfigurationProperties properties) {
        this.properties = properties;
    }

}
