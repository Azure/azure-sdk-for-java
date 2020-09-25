// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNicIpConfiguration;
import com.azure.resourcemanager.network.fluent.models.NetworkInterfaceIpConfigurationInner;

/** Implementation for NicIPConfiguration for network interfaces associated with virtual machine scale set. */
class VirtualMachineScaleSetNicIpConfigurationImpl
    extends NicIpConfigurationBaseImpl<
        VirtualMachineScaleSetNetworkInterfaceImpl, VirtualMachineScaleSetNetworkInterface>
    implements VirtualMachineScaleSetNicIpConfiguration {
    VirtualMachineScaleSetNicIpConfigurationImpl(
        NetworkInterfaceIpConfigurationInner inner,
        VirtualMachineScaleSetNetworkInterfaceImpl parent,
        NetworkManager networkManager) {
        super(inner, parent, networkManager);
    }

    // Note: The inner ipConfig contains a property with name 'publicIPAddress'
    // which is valid only when the inner is explicitly created i.e. the one
    // associated with normal virtual machines. In VMSS case the inner ipConfig
    // is implicitly created for the scale set vm instances and 'publicIPAddress'
    // property is null.
    //
}
