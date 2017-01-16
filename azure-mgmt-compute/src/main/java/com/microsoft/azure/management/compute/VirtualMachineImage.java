/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.VirtualMachineImageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure virtual machine image.
 */
@Fluent
public interface VirtualMachineImage extends
        Wrapper<VirtualMachineImageInner> {
    /**
     * @return the resource id of this image
     */
    String id();

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
     * @return the image reference representing the publisher, offer, SKU and version of the virtual machine image
     */
    ImageReference imageReference();

    /**
     * @return the purchase plan for the virtual machine image
     */
    PurchasePlan plan();

    /**
     * @return OS disk image in the virtual machine image
     */
    OSDiskImage osDiskImage();

    /**
     * @return data disk images in the virtual machine image, indexed by the disk lun
     */
    Map<Integer, DataDiskImage> dataDiskImages();
}
