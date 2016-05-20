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
 * The type representing Azure virtual machine image.
 */
public interface VirtualMachineImage extends
        Wrapper<VirtualMachineImageInner> {
    /**
     * The region in which virtual machine image is available.
     *
     * @return The region
     */
    Region location();

    /**
     * The publisher name of the virtual machine image.
     *
     * @return The publisher name
     */
    String publisher();

    /**
     * The name of the virtual machine image offer.
     *
     * @return The offer name
     */
    String offer();

    /**
     * The commercial name of the virtual machine image (SKU).
     *
     * @return The SKU name
     */
    String sku();

    /**
     * The version of the virtual machine image.
     *
     * @return The version
     */
    String version();

    /**
     * The image reference representing publisher, offer, sku and version of the virtual machine image.
     *
     * @return The image reference
     */
    ImageReference imageReference();

    /**
     * The purchase plan for the virtual machine image.
     *
     * @return The purchase plan.
     */
    PurchasePlan plan();

    /**
     * Describes the OS Disk image in the virtual machine image.
     *
     * @return The OS Disk image
     */
    OSDiskImage osDiskImage();

    /**
     * Describes the Data disk images in the virtual machine.
     *
     * @return The data disks.
     */
    List<DataDiskImage> dataDiskImages();

    /**
     * Represents a virtual image image publisher.
     */
    interface Publisher {
        /**
         * Gets the region where virtual machine images from this publisher is available.
         *
         * @return The region name
         */
        Region region();

        /**
         * Gets the name of the virtual machine image publisher.
         *
         * @return The publisher name
         */
        String publisher();

        /**
         * Lists the virtual machine image offers from this publisher in the specific region.
         *
         * @return list of virtual machine image offers
         * @throws CloudException
         * @throws IOException
         */
        List<Offer> listOffers() throws CloudException, IOException;
    }

    /**
     * Represents a virtual machine image offer.
     */
    interface Offer {
        /**
         * Gets the region where this virtual machine image offer is available.
         *
         * @return The region name
         */
        Region region();

        /**
         * Gets the publisher name of this virtual machine image offer.
         *
         * @return The publisher name
         */
        String publisher();

        /**
         * Gets the name of the virtual machine image offer.
         *
         * @return The offer name
         */
        String offer();
        List<Sku> listSkus() throws CloudException, IOException;
    }

    /**
     * Represents a virtual machine image SKU.
     */
    interface Sku {
        /**
         * Gets the region where this virtual machine image offer SKU is available.
         *
         * @return The region name
         */
        Region region();

        /**
         * Gets the publisher name of this virtual machine image offer SKU.
         *
         * @return The publisher name
         */
        String publisher();

        /**
         * Gets the virtual machine offer name that this SKU belongs to.
         *
         * @return The offer name
         */
        String offer();

        /**
         * Gets the commercial name of the virtual machine image (SKU).
         *
         * @return The SKU name
         */
        String sku();
        List<VirtualMachineImage> listImages() throws CloudException, IOException;
    }
}
