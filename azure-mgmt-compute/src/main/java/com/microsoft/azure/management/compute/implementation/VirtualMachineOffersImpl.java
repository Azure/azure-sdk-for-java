/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachineOffers;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

/**
 * The implementation for {@link VirtualMachineOffers}.
 */
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
        return new VirtualMachineOfferImpl(this.publisher, inner.name(), this.innerCollection);
    }

    @Override
    public PagedList<VirtualMachineOffer> list() throws CloudException, IllegalArgumentException, IOException {
        return wrapList(innerCollection.listOffers(publisher.region().toString(), publisher.name()).getBody());
    }
}
