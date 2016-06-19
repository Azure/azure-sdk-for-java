/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.Skus;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

/**
 * The implementation for {@link VirtualMachineImagePublishers}.
 */
class SkusImpl
        extends ReadableWrappersImpl<Sku, SkuImpl, VirtualMachineImageResourceInner>
        implements Skus {

    private final VirtualMachineImagesInner innerCollection;
    private final Offer offer;

    SkusImpl(Offer offer, VirtualMachineImagesInner innerCollection) {
        this.innerCollection = innerCollection;
        this.offer = offer;
    }

    @Override
    public PagedList<Sku> list() throws CloudException, IOException {
        return wrapList(innerCollection.listSkus(
                offer.region().toString(),
                offer.publisher().name(),
                offer.name()).getBody());
    }

    @Override
    protected SkuImpl wrapModel(VirtualMachineImageResourceInner inner) {
        return new SkuImpl(this.offer, inner.name(), innerCollection);
    }
}
