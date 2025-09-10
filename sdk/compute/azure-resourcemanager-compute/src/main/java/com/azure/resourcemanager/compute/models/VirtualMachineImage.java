// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineImageInner;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.Map;

/** An immutable client-side representation of an Azure virtual machine image. */
@Fluent
public interface VirtualMachineImage extends HasInnerModel<VirtualMachineImageInner> {
    /**
     * Gets the resource ID of this image.
     *
     * @return the resource ID of this image
     */
    String id();

    /**
     * Gets the region in which virtual machine image is available.
     *
     * @return the region in which virtual machine image is available
     */
    Region location();

    /**
     * Gets the publisher name of the virtual machine image.
     *
     * @return the publisher name of the virtual machine image
     */
    String publisherName();

    /**
     * Gets the name of the virtual machine image offer this image is part of.
     *
     * @return the name of the virtual machine image offer this image is part of
     */
    String offer();

    /**
     * Gets the commercial name of the virtual machine image (SKU).
     *
     * @return the commercial name of the virtual machine image (SKU)
     */
    String sku();

    /**
     * Gets the version of the virtual machine image.
     *
     * @return the version of the virtual machine image
     */
    String version();

    /**
     * Gets the image reference representing the publisher, offer, SKU and version of the virtual machine image.
     *
     * @return the image reference representing the publisher, offer, SKU and version of the virtual machine image
     */
    ImageReference imageReference();

    /**
     * Gets the purchase plan for the virtual machine image.
     *
     * @return the purchase plan for the virtual machine image
     */
    PurchasePlan plan();

    /**
     * Gets OS disk image in the virtual machine image.
     *
     * @return OS disk image in the virtual machine image
     */
    OSDiskImage osDiskImage();

    /**
     * Gets data disk images in the virtual machine image.
     *
     * @return data disk images in the virtual machine image, indexed by the disk LUN
     */
    Map<Integer, DataDiskImage> dataDiskImages();
}
