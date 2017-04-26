/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNicIPConfiguration;
import com.microsoft.azure.management.network.NicIPConfiguration;

/**
 *  Implementation for {@link NicIPConfiguration} for network interfaces associated
 *  with virtual machine scale set.
 */
@LangDefinition
class VirtualMachineScaleSetNicIPConfigurationImpl
        extends
        NicIPConfigurationBaseImpl<VirtualMachineScaleSetNetworkInterfaceImpl,
                VirtualMachineScaleSetNetworkInterface>
        implements
        VirtualMachineScaleSetNicIPConfiguration {
    VirtualMachineScaleSetNicIPConfigurationImpl(NetworkInterfaceIPConfigurationInner inner,
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