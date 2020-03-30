/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.Refreshable;

import java.util.Map;

/**
 * Virtual machine scale set network interface.
 */
@Fluent
public interface VirtualMachineScaleSetNetworkInterface extends
        NetworkInterfaceBase,
        Resource,
        Refreshable<VirtualMachineScaleSetNetworkInterface> {
    /**
     * @return the IP configurations of this network interface, indexed by their names
     */
    Map<String, VirtualMachineScaleSetNicIPConfiguration> ipConfigurations();

    /**
     * @return the primary IP configuration of this network interface
     */
    VirtualMachineScaleSetNicIPConfiguration primaryIPConfiguration();
}