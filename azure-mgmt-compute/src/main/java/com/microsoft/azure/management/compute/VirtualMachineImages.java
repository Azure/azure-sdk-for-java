/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

/**
 *  Entry point to virtual machine image management API.
 */
@Fluent
public interface VirtualMachineImages extends
        SupportsListingByRegion<VirtualMachineImage> {
    /**
     * Gets a virtual machine image.
     *
     * @param region the region
     * @param publisherName publisher name
     * @param offerName offer name
     * @param skuName SKU name
     * @param version version name
     * @return the virtual machine image
     */
    VirtualMachineImage getImage(Region region, String publisherName, String offerName, String skuName, String version);

    /**
     * @return entry point to virtual machine image publishers
     */
    VirtualMachinePublishers publishers();
}
