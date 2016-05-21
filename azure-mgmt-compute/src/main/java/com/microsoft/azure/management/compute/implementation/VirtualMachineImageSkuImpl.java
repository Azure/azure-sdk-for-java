package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VirtualMachineImageSkuImpl
        implements VirtualMachineImage.Sku {
    private final VirtualMachineImagesInner client;
    private final VirtualMachineImage.Offer offer;
    private final String sku;

    VirtualMachineImageSkuImpl(VirtualMachineImage.Offer offer, String sku, VirtualMachineImagesInner client) {
        this.offer = offer;
        this.sku = sku;
        this.client = client;
    }

    @Override
    public Region region() {
        return offer.region();
    }

    @Override
    public String publisher() {
        return offer.publisher();
    }

    @Override
    public String offer() {
        return offer.offer();
    }

    public String sku() {
        return this.sku;
    }

    @Override
    public List<VirtualMachineImage> listImages() throws CloudException, IOException {
        List<VirtualMachineImage> images = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner :
                client.list(
                        region().toString(),
                        publisher(),
                        offer(),
                        sku).getBody()) {
            String version = inner.name();
            images.add(new VirtualMachineImageImpl(
                    region(),
                    publisher(),
                    offer(),
                    sku,
                    version,
                    client.get(region().toString(), publisher(), offer(), sku, version).getBody(),
                    client));
        }
        return images;
    }
}
