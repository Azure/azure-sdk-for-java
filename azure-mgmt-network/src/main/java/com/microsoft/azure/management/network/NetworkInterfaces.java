/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkInterfacesInner;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to network interface management.
 */
@Fluent
public interface NetworkInterfaces  extends
        SupportsCreating<NetworkInterface.DefinitionStages.Blank>,
        SupportsListing<NetworkInterface>,
        SupportsListingByResourceGroup<NetworkInterface>,
        SupportsGettingByResourceGroup<NetworkInterface>,
        SupportsGettingById<NetworkInterface>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<NetworkInterface>,
        SupportsBatchDeletion,
        HasManager<NetworkManager>,
        HasInner<NetworkInterfacesInner> {

    /**
     * Gets a network interface associated with a virtual machine scale set instance.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @param instanceId the virtual machine scale set vm instance id
     * @param name the network interface name
     * @return network interface
     */
    VirtualMachineScaleSetNetworkInterface getByVirtualMachineScaleSetInstanceId(String resourceGroupName, String scaleSetName, String instanceId, String name);

    /**
     * List the network interfaces associated with a virtual machine scale set.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @return list of network interfaces
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSet(String resourceGroupName, String scaleSetName);

    /**
     * List the network interfaces associated with a virtual machine scale set.
     *
     * @param id virtual machine scale set resource id
     * @return list of network interfaces
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetId(String id);

    /**
     * List the network interfaces associated with a specific virtual machine instance in a scale set.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @param instanceId the virtual machine scale set vm instance id
     * @return list of network interfaces
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetInstanceId(String resourceGroupName, String scaleSetName, String instanceId);
}
