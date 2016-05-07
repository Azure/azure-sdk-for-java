package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.DataDiskImage;
import com.microsoft.azure.management.compute.implementation.api.OSDiskImage;
import com.microsoft.azure.management.compute.implementation.api.PurchasePlan;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageInner;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;

import java.util.List;

class VirtualMachineImageImpl
        extends IndexableWrapperImpl<VirtualMachineImageInner>
        implements VirtualMachineImage {
    VirtualMachineImageImpl(String publisher, String offer, String sku, String version) {
        super(null, null);
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
        this.version = version;
    }

    VirtualMachineImageImpl(String publisher, String offer, String sku, String version, VirtualMachineImageInner innerObject) {
        super(innerObject.id(), innerObject);
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
        this.version = version;
    }

    private String publisher;
    private String offer;
    private String sku;
    private String version;

    @Override
    public String location() {
        return inner().location();
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
