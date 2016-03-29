/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.models;

import java.util.List;

/**
 * Describes a virtual machine scale set network profile.
 */
public class VirtualMachineScaleSetNetworkProfile {
    /**
     * Gets or sets the list of network configurations.
     */
    private List<VirtualMachineScaleSetNetworkConfiguration> networkInterfaceConfigurations;

    /**
     * Get the networkInterfaceConfigurations value.
     *
     * @return the networkInterfaceConfigurations value
     */
    public List<VirtualMachineScaleSetNetworkConfiguration> getNetworkInterfaceConfigurations() {
        return this.networkInterfaceConfigurations;
    }

    /**
     * Set the networkInterfaceConfigurations value.
     *
     * @param networkInterfaceConfigurations the networkInterfaceConfigurations value to set
     */
    public void setNetworkInterfaceConfigurations(List<VirtualMachineScaleSetNetworkConfiguration> networkInterfaceConfigurations) {
        this.networkInterfaceConfigurations = networkInterfaceConfigurations;
    }

}
