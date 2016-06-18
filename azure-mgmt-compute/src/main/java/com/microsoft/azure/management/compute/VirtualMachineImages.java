/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
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

    /**
     * Lists the virtual machine publishers in a region.
     * @param regionName the name of the region
     * @return the list of VM image publisher
     * @throws CloudException thrown for an invalid response from the service
     * @throws IOException thrown for IO exception
     */
    List<VirtualMachineImage.Publisher> listPublishers(final String regionName) throws CloudException, IOException;
}
