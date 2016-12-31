package com.microsoft.azure.management.network;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to network interface management.
 */
@Fluent()
public interface NetworkInterfaces  extends
        SupportsCreating<NetworkInterface.DefinitionStages.Blank>,
        SupportsListing<NetworkInterface>,
        SupportsListingByGroup<NetworkInterface>,
        SupportsGettingByGroup<NetworkInterface>,
        SupportsGettingById<NetworkInterface>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<NetworkInterface> {
    /**
     * List the network interfaces associated with a virtual machine scale set.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @return list of network interfaces
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSet(String resourceGroupName, String scaleSetName);

    /**
     * List the network interfaces associated with a specific virtual machine instance in a scale set.
     *
     * @param resourceGroupName virtual machine scale set resource group name
     * @param scaleSetName scale set name
     * @param instanceId the virtual machine scale set vm instance id
     * @return list of network interfaces
     */
    PagedList<VirtualMachineScaleSetNetworkInterface> listByVirtualMachineScaleSetVMInstanceId(String resourceGroupName, String scaleSetName, String instanceId);
}
