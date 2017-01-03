/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkInterfaceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import java.util.Map;

/**
 * Virtual machine scale set network interface.
 */
@Fluent
public interface VirtualMachineScaleSetNetworkInterface extends
        NetworkInterfaceBase,
        Resource,
        Refreshable<VirtualMachineScaleSetNetworkInterface>,
        Wrapper<NetworkInterfaceInner> {
    /**
     * @return the IP configurations of this network interface, indexed by their names
     */
    Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigurations();

    /**
     * @return the primary IP configuration of this network interface
     */
    VirtualMachineScaleSetNicIpConfiguration primaryIpConfiguration();
}