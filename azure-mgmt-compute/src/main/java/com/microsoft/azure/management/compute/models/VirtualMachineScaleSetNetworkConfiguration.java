/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a virtual machine scale set network profile's network
 * configurations.
 */
public class VirtualMachineScaleSetNetworkConfiguration extends SubResource {
    /**
     * Gets or sets the network configuration name.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * The properties property.
     */
    private VirtualMachineScaleSetNetworkConfigurationProperties properties;

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
    public VirtualMachineScaleSetNetworkConfigurationProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(VirtualMachineScaleSetNetworkConfigurationProperties properties) {
        this.properties = properties;
    }

}
