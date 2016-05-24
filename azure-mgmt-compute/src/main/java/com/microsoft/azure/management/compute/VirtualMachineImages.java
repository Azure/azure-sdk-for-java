package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByLocation;

import java.io.IOException;
import java.util.List;

/**
 *  The type representing Azure virtual machine image collection.
 */
public interface VirtualMachineImages extends
        SupportsListingByLocation<VirtualMachineImage> {
    /**
     * Lists the virtual machine publishers in a region.
     *
     * @param region The region
     * @return The list of VM image publishers
     * @throws CloudException Thrown for an invalid response from the service.
     * @throws IOException Thrown for IO exception.
     */
    List<VirtualMachineImage.Publisher> listPublishers(final Region region) throws CloudException, IOException;
}
