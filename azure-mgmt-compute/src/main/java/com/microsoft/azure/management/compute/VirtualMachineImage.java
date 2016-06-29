/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.VirtualMachineImageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure virtual machine image.
 */
public interface VirtualMachineImage extends
        Wrapper<VirtualMachineImageInner> {
    /**
     * @return the region in which virtual machine image is available
     */
    Region location();

    /**
     * @return the publisher name of the virtual machine image
     */
    String publisherName();

    /**
     * @return the name of the virtual machine image offer this image is part of
     */
    String offer();

    /**
     * @return the commercial name of the virtual machine image (SKU)
     */
    String sku();

    /**
     * @return the version of the virtual machine image
     */
    String version();

    /**
     * @return the image reference representing publisher, offer, sku and version of the virtual machine image
     */
    ImageReference imageReference();

    /**
     * @return the purchase plan for the virtual machine image.
     */
    PurchasePlan plan();

    /**
     * @return description of the OS Disk image in the virtual machine image.
     */
    OSDiskImage osDiskImage();

    /**
     * @return description of the Data disk images in the virtual machine.
     */
    List<DataDiskImage> dataDiskImages();
}
