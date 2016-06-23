package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.Skus;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * The implementation for {@link Offer}.
 */
class OfferImpl implements Offer {
    private final Publisher publisher;
    private final String offerName;
    private final SkusImpl skus;

    OfferImpl(Publisher publisher, String offer, VirtualMachineImagesInner client) {
        this.publisher = publisher;
        this.offerName = offer;
        this.skus = new SkusImpl(this, client);
    }

    @Override
    public Region region() {
        return publisher.region();
    }

    @Override
    public Publisher publisher() {
        return publisher;
    }

    @Override
    public String name() {
        return this.offerName;
    }

    @Override
    public Skus skus() {
        return this.skus;
    }
}
