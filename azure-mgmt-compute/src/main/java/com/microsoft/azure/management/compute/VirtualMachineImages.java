package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByLocation;

import java.io.IOException;
import java.util.List;

public interface VirtualMachineImages extends
        SupportsListingByLocation<VirtualMachineImage> {
    /**
     * Lists the virtual machine publishers in a region.
     * @param region The region
     * @return The list of VM image publishers
     * @throws CloudException
     * @throws IOException
     */
    List<VirtualMachineImage.Publisher> listPublishers(final Region region) throws CloudException, IOException;
}
