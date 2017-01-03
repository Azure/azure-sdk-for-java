/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to virtual machine scale set network interface management API.
 */
@Fluent
public interface VirtualMachineScaleSetNetworkInterfaces extends
        SupportsListing<VirtualMachineScaleSetNetworkInterface> {
    /**
     * Lists all the network interfaces associated with a virtual machine instance in the scale set.
     *
     * @param instanceId virtual machine scale set vm instance id
     * @return list of network interfaces
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineInstanceId(String instanceId);
}