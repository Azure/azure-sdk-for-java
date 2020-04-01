/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.NetworkInterfacesInner;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;


/**
 * Entry point to virtual machine scale set network interface management API.
 */
@Fluent
public interface VirtualMachineScaleSetNetworkInterfaces extends
        SupportsListing<VirtualMachineScaleSetNetworkInterface>,
        HasInner<NetworkInterfacesInner>,
        HasManager<NetworkManager> {
    /**
     * Gets a network interface associated with a virtual machine scale set instance.
     *
     * @param instanceId the virtual machine scale set vm instance id
     * @param name       the network interface name
     * @return the network interface
     */
    VirtualMachineScaleSetNetworkInterface getByVirtualMachineInstanceId(String instanceId, String name);

    /**
     * Lists all the network interfaces associated with a virtual machine instance in the scale set.
     *
     * @param instanceId virtual machine scale set vm instance id
     * @return list of network interfaces
     */
    PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineInstanceId(String instanceId);
}