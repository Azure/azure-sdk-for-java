// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.compute.implementation;

import com.azure.management.compute.models.VirtualMachineOffer;
import com.azure.management.compute.models.VirtualMachinePublisher;
import com.azure.management.compute.models.VirtualMachineSkus;
import com.azure.management.compute.fluent.VirtualMachineImagesClient;
import com.azure.management.resources.fluentcore.arm.Region;

/** The implementation for {@link VirtualMachineOffer}. */
class VirtualMachineOfferImpl implements VirtualMachineOffer {
    private final VirtualMachinePublisher publisher;
    private final String offerName;
    private final VirtualMachineSkusImpl skus;

    VirtualMachineOfferImpl(VirtualMachinePublisher publisher, String offer, VirtualMachineImagesClient client) {
        this.publisher = publisher;
        this.offerName = offer;
        this.skus = new VirtualMachineSkusImpl(this, client);
    }

    @Override
    public Region region() {
        return publisher.region();
    }

    @Override
    public VirtualMachinePublisher publisher() {
        return publisher;
    }

    @Override
    public String name() {
        return this.offerName;
    }

    @Override
    public VirtualMachineSkus skus() {
        return this.skus;
    }
}
