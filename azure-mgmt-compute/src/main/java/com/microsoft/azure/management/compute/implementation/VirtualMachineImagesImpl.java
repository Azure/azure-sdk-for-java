package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VirtualMachineImagesImpl
        implements VirtualMachineImages {
    private final VirtualMachineImagesInner client;

    VirtualMachineImagesImpl(VirtualMachineImagesInner client) {
        this.client = client;
    }

    @Override
    public List<VirtualMachineImage.Publisher> listPublishers(final Region region) throws CloudException, IOException {
        List<VirtualMachineImage.Publisher> publishers = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner :
                client.listPublishers(region.toString()).getBody()) {
            publishers.add(new VirtualMachineImagePublisherImpl(region, inner.name(), client));
        }
        return publishers;
    }

    @Override
    public List<VirtualMachineImage> list(Region location) throws CloudException, IOException {
        List<VirtualMachineImage> images = new ArrayList<>();
        for (VirtualMachineImage.Publisher publisher : listPublishers(location)) {
            for (VirtualMachineImage.Offer offer : publisher.listOffers()) {
                for (VirtualMachineImage.Sku sku : offer.listSkus()) {
                    images.addAll(sku.listImages());
                }
            }
        }
        return images;
    }
}
