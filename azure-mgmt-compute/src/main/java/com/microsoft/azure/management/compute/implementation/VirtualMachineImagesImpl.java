package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
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
    private final VirtualMachineImagesInner client;

    VirtualMachineImagesImpl(VirtualMachineImagesInner client) {
        this.client = client;
    }

    @Override
    public List<VirtualMachineImage.Publisher> listPublishers(final Region region) throws CloudException, IOException {
        return listPublishers(region.toString());
    }

    @Override
    public List<VirtualMachineImage.Publisher> listPublishers(String regionName) throws CloudException, IOException {
        List<VirtualMachineImage.Publisher> publishers = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner
                : client.listPublishers(regionName).getBody()) {
            publishers.add(new VirtualMachineImagePublisherImpl(Region.fromName(regionName), inner.name(), client));
        }
        return Collections.unmodifiableList(publishers);
    }

    @Override
    public List<VirtualMachineImage> listByRegion(Region location) throws CloudException, IOException {
        return listByRegion(location.toString());
    }

    @Override
    public List<VirtualMachineImage> listByRegion(String regionName) throws CloudException, IOException {
        List<VirtualMachineImage> images = new ArrayList<>();
        for (VirtualMachineImage.Publisher publisher : listPublishers(regionName)) {
            for (VirtualMachineImage.Offer offer : publisher.listOffers()) {
                for (VirtualMachineImage.Sku sku : offer.listSkus()) {
                    images.addAll(sku.listImages());
                }
            }
        }
        return Collections.unmodifiableList(images);
    }

}
