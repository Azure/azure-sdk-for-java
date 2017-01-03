/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkInterfaceIPConfigurationInner;
import com.microsoft.azure.management.network.model.HasPrivateIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An IP configuration in a network interface associated with a virtual machine
 * scale set.
 */
@Fluent
public interface VirtualMachineScaleSetNicIpConfiguration extends
        NicIpConfigurationBase,
        Wrapper<NetworkInterfaceIPConfigurationInner>,
        ChildResource<VirtualMachineScaleSetNetworkInterface>,
        HasPrivateIpAddress,
        HasSubnet {
}