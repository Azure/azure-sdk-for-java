package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

/**
 *  Entry point to virtual machine extension image management API.
 */
@Fluent
public interface VirtualMachineExtensionImages extends SupportsListingByRegion<VirtualMachineExtensionImage> {
    /**
     * @return entry point to virtual machine extension image publishers
     */
    VirtualMachinePublishers publishers();
}