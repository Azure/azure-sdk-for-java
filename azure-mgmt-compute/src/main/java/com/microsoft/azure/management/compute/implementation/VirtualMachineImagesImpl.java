package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachinePublishers;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * The implementation for {@link VirtualMachineImages}.
 */
@LangDefinition
class VirtualMachineImagesImpl
        implements VirtualMachineImages {
    private final VirtualMachinePublishers publishers;
    private final VirtualMachineImagesInner client;

    VirtualMachineImagesImpl(VirtualMachinePublishers publishers, VirtualMachineImagesInner client) {
        this.publishers = publishers;
        this.client = client;
    }

    @Override
    public VirtualMachineImage getImage(Region region, String publisherName, String offerName, String skuName, String version) {
        VirtualMachineImageInner innerImage = this.client.get(region.name(),
                publisherName,
                offerName,
                skuName,
                version);
        return new VirtualMachineImageImpl(region, publisherName, offerName, skuName, version, innerImage);
    }

    @Override
    public PagedList<VirtualMachineImage> listByRegion(Region location) {
        return listByRegion(location.toString());
    }

    @Override
    public PagedList<VirtualMachineImage> listByRegion(String regionName) {
        PagedList<VirtualMachinePublisher> publishers = this.publishers().listByRegion(regionName);

        PagedList<VirtualMachineOffer> offers =
                new ChildListFlattener<>(publishers, new ChildListFlattener.ChildListLoader<VirtualMachinePublisher, VirtualMachineOffer>() {
                    @Override
                    public PagedList<VirtualMachineOffer> loadList(VirtualMachinePublisher publisher)  {
                        return publisher.offers().list();
                    }
                }).flatten();

        PagedList<VirtualMachineSku> skus =
                new ChildListFlattener<>(offers, new ChildListFlattener.ChildListLoader<VirtualMachineOffer, VirtualMachineSku>() {
                    @Override
                    public PagedList<VirtualMachineSku> loadList(VirtualMachineOffer offer)  {
                        return offer.skus().list();
                    }
                }).flatten();

        PagedList<VirtualMachineImage> images =
                new ChildListFlattener<>(skus, new ChildListFlattener.ChildListLoader<VirtualMachineSku, VirtualMachineImage>() {
                    @Override
                    public PagedList<VirtualMachineImage> loadList(VirtualMachineSku sku)  {
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