// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point to virtual machine scale set network interface management API. */
@Fluent
public interface VirtualMachineScaleSetNetworkInterfaces
    extends SupportsListing<VirtualMachineScaleSetNetworkInterface>, HasManager<NetworkManager> {
    /**
     * Gets a network interface associated with a virtual machine scale set instance.
     *
     * @param instanceId the virtual machine scale set vm instance id
     * @param name the network interface name
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

    /**
     * Lists all the network interfaces associated with a virtual machine instance in the scale set asynchronously.
     *
     * @param instanceId virtual machine scale set vm instance id
     * @return list of network interfaces
     */
    PagedFlux<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineInstanceIdAsync(String instanceId);
}
