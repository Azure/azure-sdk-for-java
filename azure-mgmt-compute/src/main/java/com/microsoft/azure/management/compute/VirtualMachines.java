package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

/**
 *  Entry point to virtual machine management API.
 */
public interface VirtualMachines extends
        SupportsListing<VirtualMachine>,
        SupportsListingByGroup<VirtualMachine>,
        SupportsGettingByGroup<VirtualMachine>,
        SupportsCreating<VirtualMachine.DefinitionBlank>,
        SupportsDeleting,
        SupportsDeletingByGroup {
    /**
     * Lists all available virtual machine sizes in a region.
     *
     * @param region The region upon which virtual-machine-sizes is queried.
     * @return the List&lt;VirtualMachineSize&gt; if successful.
     * @throws CloudException thrown for an invalid response from the service.
     * @throws IOException thrown for IO exception.
     */
    PagedList<VirtualMachineSize> listSizes(String region) throws CloudException, IOException;

    /**
     * Entry point to virtual machine management API within a specific resource group.
     */
    interface InGroup extends
            SupportsListing<VirtualMachine>,
            SupportsCreating<VirtualMachine.DefinitionBlank>,
            SupportsDeleting {
        /**
         * Lists all available virtual machine sizes in a region.
         *
         * @param region the region upon which virtual-machine-sizes is queried.
         * @return the List&lt;VirtualMachineSize&gt; if successful.
         * @throws CloudException thrown for an invalid response from the service.
         * @throws IOException thrown for IO exception.
         */
        PagedList<VirtualMachineSize> listSizes(String region) throws CloudException, IOException;
    }
}
