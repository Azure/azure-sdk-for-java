// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.VirtualMachineOffer;
import com.azure.resourcemanager.compute.models.VirtualMachineOffers;
import com.azure.resourcemanager.compute.models.VirtualMachinePublisher;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineImageResourceInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineImagesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for {@link VirtualMachineOffers}. */
class VirtualMachineOffersImpl
    extends ReadableWrappersImpl<VirtualMachineOffer, VirtualMachineOfferImpl, VirtualMachineImageResourceInner>
    implements VirtualMachineOffers {

    private final VirtualMachineImagesClient innerCollection;
    private final VirtualMachinePublisher publisher;

    VirtualMachineOffersImpl(VirtualMachineImagesClient innerCollection, VirtualMachinePublisher publisher) {
        this.innerCollection = innerCollection;
        this.publisher = publisher;
    }

    @Override
    protected VirtualMachineOfferImpl wrapModel(VirtualMachineImageResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineOfferImpl(this.publisher, inner.name(), this.innerCollection);
    }

    @Override
    public PagedIterable<VirtualMachineOffer> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<VirtualMachineOffer> listAsync() {
        return PagedConverter.mapPage(PagedConverter
            .convertListToPagedFlux(innerCollection.listOffersWithResponseAsync(
                publisher.region().toString(), publisher.name())),
            this::wrapModel);
    }
}
