// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.VirtualMachineOffer;
import com.azure.resourcemanager.compute.models.VirtualMachineSku;
import com.azure.resourcemanager.compute.models.VirtualMachineSkus;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineImageResourceInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineImagesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for {@link VirtualMachineSkus}. */
class VirtualMachineSkusImpl
    extends ReadableWrappersImpl<VirtualMachineSku, VirtualMachineSkuImpl, VirtualMachineImageResourceInner>
    implements VirtualMachineSkus {

    private final VirtualMachineImagesClient innerCollection;
    private final VirtualMachineOffer offer;

    VirtualMachineSkusImpl(VirtualMachineOffer offer, VirtualMachineImagesClient innerCollection) {
        this.innerCollection = innerCollection;
        this.offer = offer;
    }

    @Override
    public PagedIterable<VirtualMachineSku> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    protected VirtualMachineSkuImpl wrapModel(VirtualMachineImageResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineSkuImpl(this.offer, inner.name(), innerCollection);
    }

    @Override
    public PagedFlux<VirtualMachineSku> listAsync() {
        return PagedConverter
            .convertListToPagedFlux(
                innerCollection.listSkusAsync(offer.region().toString(), offer.publisher().name(), offer.name()))
            .mapPage(this::wrapModel);
    }
}
