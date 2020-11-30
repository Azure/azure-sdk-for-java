// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingByRegion;

/** Entry point to virtual machine image management API. */
@Fluent
public interface VirtualMachineImages extends SupportsListingByRegion<VirtualMachineImage> {
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
     * Gets a virtual machine image.
     *
     * @param region the region
     * @param publisherName publisher name
     * @param offerName offer name
     * @param skuName SKU name
     * @param version version name
     * @return the virtual machine image
     */
    VirtualMachineImage getImage(String region, String publisherName, String offerName, String skuName, String version);

    /** @return entry point to virtual machine image publishers */
    VirtualMachinePublishers publishers();
}
