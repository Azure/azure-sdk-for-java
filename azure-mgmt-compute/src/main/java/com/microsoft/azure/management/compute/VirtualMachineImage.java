package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.io.IOException;
import java.util.List;

public interface VirtualMachineImage extends
        Wrapper<VirtualMachineImageInner> {
    Region location();
    String publisher();
    String offer();
    String sku();
    String version();
    ImageReference imageReference();
    PurchasePlan plan();
    OSDiskImage osDiskImage();
    List<DataDiskImage> dataDiskImages();

    interface Publisher {
        /**
         * Gets the region where virtual machine images from this publisher is available
         * @return The region name
         */
        Region region();

        /**
         * Gets the name of the virtual machine image publisher
         * @return The publisher name
         */
        String publisher();

        /**
         * Lists the virtual machine image offers from this publisher in the specific region
         * @return list of virtual machine image offers
         * @throws CloudException
         * @throws IOException
         */
        List<Offer> listOffers() throws CloudException, IOException;
    }

    interface Offer {
        /**
         * Gets the region where this virtual machine image offer is available
         * @return The region name
         */
        Region region();

        /**
         * Gets the publisher name of this virtual machine image offer
         * @return The publisher name
         */
        String publisher();

        /**
         * Gets the name of the virtual machine image offer
         * @return The offer name
         */
        String offer();
        List<Sku> listSkus() throws CloudException, IOException;
    }

    interface Sku {
        /**
         * Gets the region where this virtual machine image offer SKU is available
         * @return The region name
         */
        Region region();

        /**
         * Gets the publisher name of this virtual machine image offer SKU
         * @return The publisher name
         */
        String publisher();

        /**
         * Gets the virtual machine offer name that this SKU belongs to
         * @return The offer name
         */
        String offer();

        /**
         * Gets the commercial name of the virtual machine image (SKU)
         * @return The SKU name
         */
        String sku();
        List<VirtualMachineImage> listImages() throws CloudException, IOException;
    }
}
