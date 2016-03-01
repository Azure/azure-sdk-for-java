/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Describes a virtual machine scale set network profile's IP configuration.
 */
public class VirtualMachineScaleSetNetworkConfigurationProperties {
    /**
     * Gets or sets whether this is a primary NIC on a virtual machine.
     */
    private Boolean primary;

    /**
     * Gets or sets the virtual machine scale set IP Configuration.
     */
    @JsonProperty(required = true)
    private List<VirtualMachineScaleSetIPConfiguration> ipConfigurations;

    /**
     * Get the primary value.
     *
     * @return the primary value
     */
    public Boolean getPrimary() {
        return this.primary;
    }

    /**
     * Set the primary value.
     *
     * @param primary the primary value to set
     */
    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    /**
     * Get the ipConfigurations value.
     *
     * @return the ipConfigurations value
     */
    public List<VirtualMachineScaleSetIPConfiguration> getIpConfigurations() {
        return this.ipConfigurations;
    }

    /**
     * Set the ipConfigurations value.
     *
     * @param ipConfigurations the ipConfigurations value to set
     */
    public void setIpConfigurations(List<VirtualMachineScaleSetIPConfiguration> ipConfigurations) {
        this.ipConfigurations = ipConfigurations;
    }

}
