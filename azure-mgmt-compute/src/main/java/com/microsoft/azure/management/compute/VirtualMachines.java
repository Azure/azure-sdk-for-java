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
 *  The type representing Azure virtual machine collection.
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
     * @throws CloudException Thrown for an invalid response from the service.
     * @throws IOException Thrown for IO exception.
     */
    PagedList<VirtualMachineSize> listSizes(String region) throws CloudException, IOException;

    /**
     * A type representing Azure virtual machine under a resource gorup.
     */
    interface InGroup extends
            SupportsListing<VirtualMachine>,
            SupportsCreating<VirtualMachine.DefinitionBlank>,
            SupportsDeleting {
        /**
         * Lists all available virtual machine sizes in a region.
         *
         * @param region The region upon which virtual-machine-sizes is queried.
         * @return the List&lt;VirtualMachineSize&gt; if successful.
         * @throws CloudException
         * @throws IOException
         */
        PagedList<VirtualMachineSize> listSizes(String region) throws CloudException, IOException;
    }
}
