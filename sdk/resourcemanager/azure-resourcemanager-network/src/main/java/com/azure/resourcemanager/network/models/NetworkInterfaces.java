// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;

/** Entry point to network interface management. */
@Fluent
public interface NetworkInterfaces
    extends SupportsCreating<NetworkInterface.DefinitionStages.Blank>,
        SupportsListing<NetworkInterface>,
        SupportsListingByResourceGroup<NetworkInterface>,
        SupportsGettingByResourceGroup<NetworkInterface>,
        SupportsGettingById<NetworkInterface>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<NetworkInterface>,
        SupportsBatchDeletion,
        HasManager<NetworkManager> {

    /**
     * Gets a network interface associated with a virtual machine scale set instance.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @param instanceId the virtual machine scale set vm instance id
     * @param name the network interface name
     * @return network interface
     */
    VirtualMachineScaleSetNetworkInterface getByVirtualMachineScaleSetInstanceId(
        String resourceGroupName, String scaleSetName, String instanceId, String name);

    /**
     * List the network interfaces associated with a virtual machine scale set.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @return list of network interfaces
     */
    PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSet(
        String resourceGroupName, String scaleSetName);

    /**
     * List the network interfaces associated with a virtual machine scale set.
     *
     * @param id virtual machine scale set resource id
     * @return list of network interfaces
     */
    PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetId(String id);

    /**
     * List the network interfaces associated with a specific virtual machine instance in a scale set.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @param instanceId the virtual machine scale set vm instance id
     * @return list of network interfaces
     */
    PagedIterable<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetInstanceId(
        String resourceGroupName, String scaleSetName, String instanceId);

    /**
     * List the network interfaces associated with a specific virtual machine instance in a scale set asynchronously.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @param instanceId the virtual machine scale set vm instance id
     * @return list of network interfaces
     */
    PagedFlux<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetInstanceIdAsync(
        String resourceGroupName, String scaleSetName, String instanceId);

    /**
     * Begins deleting a virtual machine from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the virtual machine to delete
     * @return the accepted deleting operation
     */
    Accepted<Void> beginDeleteById(String id);

    /**
     * Begins deleting a virtual machine from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name the virtual machine name
     * @return the accepted deleting operation
     */
    Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name);
}
