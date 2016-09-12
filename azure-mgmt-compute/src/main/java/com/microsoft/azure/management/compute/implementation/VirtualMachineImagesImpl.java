package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachinePublishers;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import java.io.IOException;

/**
 * The implementation for {@link VirtualMachineImages}.
 */
class VirtualMachineImagesImpl
        implements VirtualMachineImages {
    private final VirtualMachinePublishers publishers;

    VirtualMachineImagesImpl(VirtualMachinePublishers publishers) {
        this.publishers = publishers;
    }

    @Override
    public PagedList<VirtualMachineImage> listByRegion(Region location) throws CloudException, IOException {
        return listByRegion(location.toString());
    }

    @Override
    public PagedList<VirtualMachineImage> listByRegion(String regionName) throws CloudException, IOException {
        PagedList<VirtualMachinePublisher> publishers = this.publishers().listByRegion(regionName);

        PagedList<VirtualMachineOffer> offers =
                new ChildListFlattener<>(publishers, new ChildListFlattener.ChildListLoader<VirtualMachinePublisher, VirtualMachineOffer>() {
                    @Override
                    public PagedList<VirtualMachineOffer> loadList(VirtualMachinePublisher publisher) throws CloudException, IOException  {
                        return publisher.offers().list();
                    }
                }).flatten();

        PagedList<VirtualMachineSku> skus =
                new ChildListFlattener<>(offers, new ChildListFlattener.ChildListLoader<VirtualMachineOffer, VirtualMachineSku>() {
                    @Override
                    public PagedList<VirtualMachineSku> loadList(VirtualMachineOffer offer) throws CloudException, IOException  {
                        return offer.skus().list();
                    }
                }).flatten();

        PagedList<VirtualMachineImage> images =
                new ChildListFlattener<>(skus, new ChildListFlattener.ChildListLoader<VirtualMachineSku, VirtualMachineImage>() {
                    @Override
                    public PagedList<VirtualMachineImage> loadList(VirtualMachineSku sku) throws CloudException, IOException  {
                        return sku.images().list();
                    }
                }).flatten();

        return images;
    }

    @Override
    public VirtualMachinePublishers publishers() {
        return this.publishers;
    }

}