package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;

import java.util.List;

class VirtualMachineImageImpl
        extends IndexableWrapperImpl<VirtualMachineImageInner>
        implements VirtualMachineImage {
    private final VirtualMachineImagesInner client;
    private final Region location;
    private String publisher;
    private String offer;
    private String sku;
    private String version;

    VirtualMachineImageImpl(Region location, String publisher, String offer, String sku, String version, VirtualMachineImagesInner client) {
        super(null, null);
        this.location = location;
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
        this.version = version;
        this.client = client;
    }

    VirtualMachineImageImpl(Region location, String publisher, String offer, String sku, String version, VirtualMachineImageInner innerModel, VirtualMachineImagesInner client) {
        super(innerModel.id(), innerModel);
        this.location = location;
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
        this.version = version;
        this.client = client;
    }

    @Override
    public Region location() {
        return location;
    }

    @Override
    public String publisher() {
        return publisher;
    }

    @Override
    public String offer() {
        return offer;
    }

    @Override
    public String sku() {
        return sku;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public PurchasePlan plan() {
        return inner().plan();
    }

    @Override
    public OSDiskImage osDiskImage() {
        return inner().osDiskImage();
    }

    @Override
    public List<DataDiskImage> dataDiskImages() {
        return inner().dataDiskImages();
    }
}
