package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

import java.io.IOException;

/**
 *  Entry point to virtual machine extension image management API.
 */
public interface VirtualMachineExtensionImages extends SupportsListingByRegion<VirtualMachineExtensionImage> {
    /**
     * @return entry point to virtual machine extension image publishers
     */
    VirtualMachinePublishers publishers();

    /**
     * Lists all the virtual machine extension images available in a given region.
     * <p>
     * Note this is a very long running call, as it enumerates through all publishers, types and versions.
     * @return list of virtual machine extension images
     * @param regionName the name of the region as used internally by Azure
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    PagedList<VirtualMachineExtensionImage> listByRegion(String regionName) throws CloudException, IOException;

    /**
     * Lists all the virtual machine extension images available in a given region.
     * <p>
     * Note this is a very long running call, as it enumerates through all publishers, types and versions.
     * @return list of virtual machine extension images
     * @param region the region to list the images from
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    PagedList<VirtualMachineExtensionImage> listByRegion(Region region) throws CloudException, IOException;
}
