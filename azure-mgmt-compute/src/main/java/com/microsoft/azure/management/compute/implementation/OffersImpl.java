/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Offers;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

/**
 * The implementation for {@link VirtualMachineImagePublishers}.
 */
class OffersImpl
        extends ReadableWrappersImpl<Offer, OfferImpl, VirtualMachineImageResourceInner>
        implements Offers {

    private final VirtualMachineImagesInner innerCollection;
    private final Publisher publisher;

    OffersImpl(VirtualMachineImagesInner innerCollection, Publisher publisher) {
        this.innerCollection = innerCollection;
        this.publisher = publisher;
    }

    @Override
    protected OfferImpl wrapModel(VirtualMachineImageResourceInner inner) {
        return new OfferImpl(this.publisher, inner.name(), this.innerCollection);
    }

    @Override
    public PagedList<Offer> list() throws CloudException, IllegalArgumentException, IOException {
        return wrapList(innerCollection.listOffers(publisher.region().toString(), publisher.name()).getBody());
    }
}
