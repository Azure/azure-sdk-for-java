package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for {@link VirtualMachineImage.Offer}.
 */
class OfferImpl implements Offer {
    private final VirtualMachineImagesInner client;
    private final VirtualMachineImage.Publisher publisher;
    private final String offerName;

    OfferImpl(VirtualMachineImage.Publisher publisher, String offer, VirtualMachineImagesInner client) {
        this.publisher = publisher;
        this.offerName = offer;
        this.client = client;
    }

    @Override
    public Region region() {
        return publisher.region();
    }

    @Override
    public VirtualMachineImage.Publisher publisher() {
        return publisher;
    }

    @Override
    public String name() {
        return this.offerName;
    }

    @Override
    public List<VirtualMachineImage.Sku> listSkus() throws CloudException, IOException {
        List<VirtualMachineImage.Sku> skus = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner
                : client.listSkus(region().toString(), publisher().name(), name()).getBody()) {
            skus.add(new VirtualMachineImageSkuImpl(this, inner.name(), client));
        }
        return skus;
    }
}
