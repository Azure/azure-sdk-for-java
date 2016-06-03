/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.SubResource;

/**
 * Describes a virtual machine scale set network profile's network
 * configurations.
 */
@JsonFlatten
public class VirtualMachineScaleSetNetworkConfiguration extends SubResource {
    /**
     * Gets or sets the network configuration name.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets whether this is a primary NIC on a virtual machine.
     */
    @JsonProperty(value = "properties.primary")
    private Boolean primary;

    /**
     * Gets or sets the virtual machine scale set IP Configuration.
     */
    @JsonProperty(value = "properties.ipConfigurations", required = true)
    private List<VirtualMachineScaleSetIPConfiguration> ipConfigurations;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the VirtualMachineScaleSetNetworkConfiguration object itself.
     */
    public VirtualMachineScaleSetNetworkConfiguration withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the primary value.
     *
     * @return the primary value
     */
    public Boolean primary() {
        return this.primary;
    }

    /**
     * Set the primary value.
     *
     * @param primary the primary value to set
     * @return the VirtualMachineScaleSetNetworkConfiguration object itself.
     */
    public VirtualMachineScaleSetNetworkConfiguration withPrimary(Boolean primary) {
        this.primary = primary;
        return this;
    }

    /**
     * Get the ipConfigurations value.
     *
     * @return the ipConfigurations value
     */
    public List<VirtualMachineScaleSetIPConfiguration> ipConfigurations() {
        return this.ipConfigurations;
    }

    /**
     * Set the ipConfigurations value.
     *
     * @param ipConfigurations the ipConfigurations value to set
     * @return the VirtualMachineScaleSetNetworkConfiguration object itself.
     */
    public VirtualMachineScaleSetNetworkConfiguration withIpConfigurations(List<VirtualMachineScaleSetIPConfiguration> ipConfigurations) {
        this.ipConfigurations = ipConfigurations;
        return this;
    }

}
