package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByLocation;

import java.io.IOException;
import java.util.List;

public interface VirtualMachineImages extends
        SupportsListingByLocation<VirtualMachineImage> {
    List<VirtualMachineImage.Publisher> listPublishers(final Region location) throws CloudException, IOException;
}
