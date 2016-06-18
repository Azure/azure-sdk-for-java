package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for {@link VirtualMachineImage.Sku}.
 */
class VirtualMachineImageSkuImpl
        implements VirtualMachineImage.Sku {
    private final VirtualMachineImagesInner client;
    private final Offer offer;
    private final String sku;

    VirtualMachineImageSkuImpl(Offer offer, String sku, VirtualMachineImagesInner client) {
        this.offer = offer;
        this.sku = sku;
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
    public String offerName() {
        return offer.name();
    }

    public String sku() {
        return this.sku;
    }

    @Override
    public List<VirtualMachineImage> listImages() throws CloudException, IOException {
        List<VirtualMachineImage> images = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner
                : client.list(
                        region().toString(),
                        publisher().name(),
                        offerName(),
                        sku).getBody()) {
            String version = inner.name();
            images.add(new VirtualMachineImageImpl(
                    region(),
                    publisher().name(),
                    offerName(),
                    sku,
                    version,
                    client.get(region().toString(), publisher().name(), offerName(), sku, version).getBody(),
                    client));
        }
        return images;
    }
}
