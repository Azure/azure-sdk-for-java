package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation for {@link VirtualMachineImages}.
 */
class VirtualMachineImagesImpl
        implements VirtualMachineImages {
    private final VirtualMachineImages.Publishers publishers;

    VirtualMachineImagesImpl(VirtualMachineImagesInner client) {
        this.publishers = new PublishersImpl(client);
    }

    @Override
    public List<VirtualMachineImage> listByRegion(Region location) throws CloudException, IOException {
        return listByRegion(location.toString());
    }

    @Override
    public List<VirtualMachineImage> listByRegion(String regionName) throws CloudException, IOException {
        List<VirtualMachineImage> images = new ArrayList<>();
        for (Publisher publisher : this.publishers().listByRegion(regionName)) {
            for (Offer offer : publisher.offers().list()) {
                for (VirtualMachineImage.Sku sku : offer.listSkus()) {
                    images.addAll(sku.listImages());
                }
            }
        }
        return Collections.unmodifiableList(images);
    }

    @Override
    public Publishers publishers() {
        return this.publishers;
    }

}
