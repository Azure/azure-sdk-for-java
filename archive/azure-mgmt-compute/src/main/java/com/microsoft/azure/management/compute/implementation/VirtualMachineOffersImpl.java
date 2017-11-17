/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachineOffers;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

/**
 * The implementation for {@link VirtualMachineOffers}.
 */
@LangDefinition
class VirtualMachineOffersImpl
        extends ReadableWrappersImpl<VirtualMachineOffer, VirtualMachineOfferImpl, VirtualMachineImageResourceInner>
        implements VirtualMachineOffers {

    private final VirtualMachineImagesInner innerCollection;
    private final VirtualMachinePublisher publisher;

    VirtualMachineOffersImpl(VirtualMachineImagesInner innerCollection, VirtualMachinePublisher publisher) {
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
    public PagedList<VirtualMachineOffer> list() {
        return wrapList(innerCollection.listOffers(publisher.region().toString(), publisher.name()));
    }

    @Override
    public Observable<VirtualMachineOffer> listAsync() {
        return wrapListAsync(innerCollection.listOffersAsync(publisher.region().toString(), publisher.name()));
    }
}
