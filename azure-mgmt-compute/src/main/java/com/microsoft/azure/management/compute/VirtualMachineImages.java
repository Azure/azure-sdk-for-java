package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

import java.io.IOException;
import java.util.List;

/**
 *  Entry point to virtual machine image management API.
 */
public interface VirtualMachineImages extends
        SupportsListingByRegion<VirtualMachineImage> {
    /**
     * Lists the virtual machine publishers in a region.
     *
     * @param region the region
     * @return the list of VM image publishers
     * @throws CloudException thrown for an invalid response from the service
     * @throws IOException thrown for IO exception
     */
    List<VirtualMachineImage.Publisher> listPublishers(final Region region) throws CloudException, IOException;
}
