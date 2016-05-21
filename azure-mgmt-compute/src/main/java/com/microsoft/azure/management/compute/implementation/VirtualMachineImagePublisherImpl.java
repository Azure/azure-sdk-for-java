package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageResourceInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VirtualMachineImagePublisherImpl
        implements VirtualMachineImage.Publisher {
    private final VirtualMachineImagesInner client;
    private final Region location;
    private String publisher;

    VirtualMachineImagePublisherImpl(Region location, String publisher, VirtualMachineImagesInner client) {
        this.location = location;
        this.publisher = publisher;
        this.client = client;
    }

    @Override
    public Region region() {
        return location;
    }

    @Override
    public String publisher() {
        return publisher;
    }

    @Override
    public List<VirtualMachineImage.Offer> listOffers() throws CloudException, IOException {
        List<VirtualMachineImage.Offer> offers = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner :
                client.listOffers(region().toString(), publisher()).getBody()) {
            offers.add(new VirtualMachineImageOfferImpl(this, inner.name(), client));
        }
        return offers;
    }
}
