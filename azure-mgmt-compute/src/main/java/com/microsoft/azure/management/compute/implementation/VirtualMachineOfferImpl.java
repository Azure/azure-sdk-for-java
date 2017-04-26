/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachineSkus;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * The implementation for {@link VirtualMachineOffer}.
 */
@LangDefinition
class VirtualMachineOfferImpl implements VirtualMachineOffer {
    private final VirtualMachinePublisher publisher;
    private final String offerName;
    private final VirtualMachineSkusImpl skus;

    VirtualMachineOfferImpl(VirtualMachinePublisher publisher, String offer, VirtualMachineImagesInner client) {
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
