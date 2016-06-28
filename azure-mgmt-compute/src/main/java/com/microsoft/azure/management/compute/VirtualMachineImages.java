/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import java.io.IOException;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

/**
 *  Entry point to virtual machine image management API.
 */
public interface VirtualMachineImages extends
        SupportsListingByRegion<VirtualMachineImage> {
    /**
     * @return entry point to virtual machine image publishers
     */
    VirtualMachinePublishers publishers();

    /**
     * Lists all the virtual machine images available in a given region.
     * <p>
     * Note this is a very long running call, as it enumerates through all publishers, offers and skus.
     * @return list of virtual machine images
     * @param regionName the name of the region as used internally by Azure
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    PagedList<VirtualMachineImage> listByRegion(String regionName) throws CloudException, IOException;

    /**
     * Lists all the virtual machine images available in a given region.
     * <p>
     * Note this is a very long running call, as it enumerates through all publishers, offers and skus.
     * @return list of virtual machine images
     * @param region the region to list the images from
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    PagedList<VirtualMachineImage> listByRegion(Region region) throws CloudException, IOException;
}
