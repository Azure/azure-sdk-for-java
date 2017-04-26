/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.VirtualMachineImagesInSku;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * The implementation for {@link Sku}.
 */
@LangDefinition
class VirtualMachineSkuImpl
        implements VirtualMachineSku {
    private final VirtualMachineOffer offer;
    private final String skuName;
    private final VirtualMachineImagesInSku imagesInSku;

    VirtualMachineSkuImpl(VirtualMachineOffer offer, String skuName, VirtualMachineImagesInner client) {
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
