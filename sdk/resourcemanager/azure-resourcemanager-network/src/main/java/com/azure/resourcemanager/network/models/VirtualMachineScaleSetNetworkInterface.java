// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import java.util.Map;

/** Virtual machine scale set network interface. */
@Fluent
public interface VirtualMachineScaleSetNetworkInterface
    extends NetworkInterfaceBase, Resource, Refreshable<VirtualMachineScaleSetNetworkInterface> {
    /** @return the IP configurations of this network interface, indexed by their names */
    Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigurations();

    /** @return the primary IP configuration of this network interface */
    VirtualMachineScaleSetNicIpConfiguration primaryIPConfiguration();
}
