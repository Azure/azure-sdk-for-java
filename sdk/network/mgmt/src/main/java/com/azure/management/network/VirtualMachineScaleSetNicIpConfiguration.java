// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.HasPrivateIpAddress;
import com.azure.management.network.models.NetworkInterfaceIpConfigurationInner;
import com.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.azure.management.resources.fluentcore.model.HasInner;

/** An IP configuration in a network interface associated with a virtual machine scale set. */
@Fluent
public interface VirtualMachineScaleSetNicIpConfiguration
    extends NicIpConfigurationBase,
        HasInner<NetworkInterfaceIpConfigurationInner>,
        ChildResource<VirtualMachineScaleSetNetworkInterface>,
    HasPrivateIpAddress,
        HasSubnet {
}
