/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.HasPrivateIPAddress;
import com.azure.management.network.models.NetworkInterfaceIPConfigurationInner;
import com.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.azure.management.resources.fluentcore.model.HasInner;


/**
 * An IP configuration in a network interface associated with a virtual machine
 * scale set.
 */
@Fluent
public interface VirtualMachineScaleSetNicIPConfiguration extends
        NicIPConfigurationBase,
        HasInner<NetworkInterfaceIPConfigurationInner>,
        ChildResource<VirtualMachineScaleSetNetworkInterface>,
        HasPrivateIPAddress,
        HasSubnet {
}