package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.VirtualMachineImagesInSku;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * The implementation for {@link Sku}.
 */
class SkuImpl
        implements Sku {
    private final Offer offer;
    private final String skuName;
    private final VirtualMachineImagesInSku imagesInSku;

    SkuImpl(Offer offer, String skuName, VirtualMachineImagesInner client) {
        this.offer = offer;
        this.skuName = skuName;
        this.imagesInSku = new VirtualMachineImagesInSkuImpl(this, client);
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
    public VirtualMachineImagesInSku images() {
        return this.imagesInSku;
    }
}
