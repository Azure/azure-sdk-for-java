package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VirtualMachineImageOfferImpl
        implements VirtualMachineImage.Offer {
    private final VirtualMachineImagesInner client;
    private final VirtualMachineImage.Publisher publisher;
    private final String offer;

    VirtualMachineImageOfferImpl(VirtualMachineImage.Publisher publisher, String offer, VirtualMachineImagesInner client) {
        this.publisher = publisher;
        this.offer = offer;
        this.client = client;
    }

    @Override
    public Region region() {
        return publisher.region();
    }

    @Override
    public String publisher() {
        return publisher.publisher();
    }

    @Override
    public String offer() {
        return this.offer;
    }

    @Override
    public List<VirtualMachineImage.Sku> listSkus() throws CloudException, IOException {
        List<VirtualMachineImage.Sku> skus = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner :
                client.listSkus(region().toString(), publisher(), offer()).getBody()) {
            skus.add(new VirtualMachineImageSkuImpl(this, inner.name(), client));
        }
        return skus;
    }
}
