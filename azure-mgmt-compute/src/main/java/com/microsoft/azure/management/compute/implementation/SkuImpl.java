package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for {@link Sku}.
 */
class SkuImpl
        implements Sku {
    private final VirtualMachineImagesInner client;
    private final Offer offer;
    private final String skuName;

    SkuImpl(Offer offer, String skuName, VirtualMachineImagesInner client) {
        this.offer = offer;
        this.skuName = skuName;
        this.client = client;
    }

    @Override
    public Region region() {
        return offer.region();
    }

    @Override
    public Publisher publisher() {
        return offer.publisher();
    }

    @Override
    public Offer offer() {
        return offer;
    }

    public String name() {
        return this.skuName;
    }

    @Override
    public List<VirtualMachineImage> listImages() throws CloudException, IOException {
        List<VirtualMachineImage> images = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner
                : client.list(
                        region().toString(),
                        publisher().name(),
                        offer.name(),
                        skuName).getBody()) {
            String version = inner.name();
            images.add(new VirtualMachineImageImpl(
                    region(),
                    publisher().name(),
                    offer.name(),
                    skuName,
                    version,
                    client.get(region().toString(), publisher().name(), offer.name(), skuName, version).getBody(),
                    client));
        }
        return images;
    }
}
