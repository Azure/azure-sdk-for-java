package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.Publishers;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import java.io.IOException;

/**
 * The implementation for {@link VirtualMachineImages}.
 */
class VirtualMachineImagesImpl
        implements VirtualMachineImages {
    private final Publishers publishers;

    VirtualMachineImagesImpl(VirtualMachineImagesInner client) {
        this.publishers = new PublishersImpl(client);
    }

    @Override
    public PagedList<VirtualMachineImage> listByRegion(Region location) throws CloudException, IOException {
        return listByRegion(location.toString());
    }

    @Override
    public PagedList<VirtualMachineImage> listByRegion(String regionName) throws CloudException, IOException {
        PagedList<Publisher> publishers = this.publishers().listByRegion(regionName);

        PagedList<Offer> offers =
                new ChildListFlattener<>(publishers, new ChildListFlattener.ChildListLoader<Publisher, Offer>() {
                    @Override
                    public PagedList<Offer> loadList(Publisher publisher) throws CloudException, IOException  {
                        return publisher.offers().list();
                    }
                }).flatten();

        PagedList<Sku> skus =
                new ChildListFlattener<>(offers, new ChildListFlattener.ChildListLoader<Offer, Sku>() {
                    @Override
                    public PagedList<Sku> loadList(Offer offer) throws CloudException, IOException  {
                        return offer.skus().list();
                    }
                }).flatten();

        PagedList<VirtualMachineImage> images =
                new ChildListFlattener<>(skus, new ChildListFlattener.ChildListLoader<Sku, VirtualMachineImage>() {
                    @Override
                    public PagedList<VirtualMachineImage> loadList(Sku sku) throws CloudException, IOException  {
                        return sku.images().list();
                    }
                }).flatten();

        return images;
    }

    @Override
    public Publishers publishers() {
        return this.publishers;
    }

}
