/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.implementation.api.ImageReference;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageInner;
import com.microsoft.azure.management.compute.implementation.api.PurchasePlan;
import com.microsoft.azure.management.compute.implementation.api.OSDiskImage;
import com.microsoft.azure.management.compute.implementation.api.DataDiskImage;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.io.IOException;
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

    /**
     * Represents a virtual machine image SKU.
     */
    interface Sku {
        /**
         * @return the region where this virtual machine image offer SKU is available
         */
        Region region();

        /**
         * @return the publisher of this virtual machine image offer SKU
         */
        Publisher publisher();

        /**
         * @return the virtual machine offer name that this SKU belongs to
         */
        String offerName();

        /**
         * @return the commercial name of the virtual machine image (SKU)
         */
        String sku();

        /**
         * Lists the virtual machines in this SKU.
         *
         * @return the virtual machine images
         * @throws CloudException thrown for an invalid response from the service
         * @throws IOException thrown for IO exception
         */
        List<VirtualMachineImage> listImages() throws CloudException, IOException;
    }
}
