/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachineSkus;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

/**
 * The implementation for {@link VirtualMachineSkus}.
 */
class VirtualMachineSkusImpl
        extends ReadableWrappersImpl<VirtualMachineSku, VirtualMachineSkuImpl, VirtualMachineImageResourceInner>
        implements VirtualMachineSkus {

    private final VirtualMachineImagesInner innerCollection;
    private final VirtualMachineOffer offer;

    VirtualMachineSkusImpl(VirtualMachineOffer offer, VirtualMachineImagesInner innerCollection) {
        this.innerCollection = innerCollection;
        this.offer = offer;
    }

    @Override
    public PagedList<VirtualMachineSku> list() throws CloudException, IOException {
        return wrapList(innerCollection.listSkus(
                offer.region().toString(),
                offer.publisher().name(),
                offer.name()));
    }

    @Override
    protected VirtualMachineSkuImpl wrapModel(VirtualMachineImageResourceInner inner) {
        return new VirtualMachineSkuImpl(this.offer, inner.name(), innerCollection);
    }
}
