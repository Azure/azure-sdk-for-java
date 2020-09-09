// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineImageInner;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.Map;

/** An immutable client-side representation of an Azure virtual machine image. */
@Fluent
public interface VirtualMachineImage extends HasInner<VirtualMachineImageInner> {
    /** @return the resource ID of this image */
    String id();

    /** @return the region in which virtual machine image is available */
    Region location();

    /** @return the publisher name of the virtual machine image */
    String publisherName();

    /** @return the name of the virtual machine image offer this image is part of */
    String offer();

    /** @return the commercial name of the virtual machine image (SKU) */
    String sku();

    /** @return the version of the virtual machine image */
    String version();

    /** @return the image reference representing the publisher, offer, SKU and version of the virtual machine image */
    ImageReference imageReference();

    /** @return the purchase plan for the virtual machine image */
    PurchasePlan plan();

    /** @return OS disk image in the virtual machine image */
    OSDiskImage osDiskImage();

    /** @return data disk images in the virtual machine image, indexed by the disk LUN */
    Map<Integer, DataDiskImage> dataDiskImages();
}
