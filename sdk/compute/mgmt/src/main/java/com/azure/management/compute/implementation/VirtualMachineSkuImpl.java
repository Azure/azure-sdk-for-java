// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.compute.implementation;

import com.azure.management.compute.models.Sku;
import com.azure.management.compute.models.VirtualMachineImagesInSku;
import com.azure.management.compute.models.VirtualMachineOffer;
import com.azure.management.compute.models.VirtualMachinePublisher;
import com.azure.management.compute.models.VirtualMachineSku;
import com.azure.management.compute.fluent.VirtualMachineImagesClient;
import com.azure.management.resources.fluentcore.arm.Region;

/** The implementation for {@link Sku}. */
class VirtualMachineSkuImpl implements VirtualMachineSku {
    private final VirtualMachineOffer offer;
    private final String skuName;
    private final VirtualMachineImagesInSku imagesInSku;

    VirtualMachineSkuImpl(VirtualMachineOffer offer, String skuName, VirtualMachineImagesClient client) {
        this.offer = offer;
        this.skuName = skuName;
        this.imagesInSku = new VirtualMachineImagesInSkuImpl(this, client);
    }

    @Override
    public Region region() {
        return offer.region();
    }

    @Override
    public VirtualMachinePublisher publisher() {
        return offer.publisher();
    }

    @Override
    public VirtualMachineOffer offer() {
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
