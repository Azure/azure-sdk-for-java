/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.VirtualMachineImages.Offers;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * The implementation for {@link VirtualMachineImage.Publisher}.
 */
class PublisherImpl
        implements Publisher {
    private final Region location;
    private final String publisher;
    private final Offers offers;

    PublisherImpl(Region location, String publisher, VirtualMachineImagesInner client) {
        this.location = location;
        this.publisher = publisher;
        this.offers = new OffersImpl(client, this);
    }

    @Override
    public Region region() {
        return location;
    }

    @Override
    public String name() {
        return publisher;
    }

    @Override
    public Offers offers() {
        return this.offers;
    }
}
