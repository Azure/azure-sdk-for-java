// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.DataDiskImage;
import com.azure.resourcemanager.compute.models.ImageReference;
import com.azure.resourcemanager.compute.models.OSDiskImage;
import com.azure.resourcemanager.compute.models.PurchasePlan;
import com.azure.resourcemanager.compute.models.VirtualMachineImage;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineImageInner;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** The implementation for {@link VirtualMachineImage}. */
class VirtualMachineImageImpl extends IndexableWrapperImpl<VirtualMachineImageInner> implements VirtualMachineImage {
    private final Region location;
    private ImageReference imageReference;

    VirtualMachineImageImpl(Region location, String publisher, String offer, String sku, String version) {
        super(null);
        this.location = location;
        this.imageReference = new ImageReference();
        this.imageReference.withPublisher(publisher);
        this.imageReference.withOffer(offer);
        this.imageReference.withSku(sku);
        this.imageReference.withVersion(version);
    }

    VirtualMachineImageImpl(
        Region location,
        String publisher,
        String offer,
        String sku,
        String version,
        VirtualMachineImageInner innerModel) {
        super(innerModel);
        this.location = location;
        this.imageReference = new ImageReference();
        this.imageReference.withPublisher(publisher);
        this.imageReference.withOffer(offer);
        this.imageReference.withSku(sku);
        this.imageReference.withVersion(version);
    }

    @Override
    public String id() {
        if (this.innerModel() == null) {
            return null;
        }
        return this.innerModel().id();
    }

    @Override
    public Region location() {
        return location;
    }

    @Override
    public String publisherName() {
        return imageReference.publisher();
    }

    @Override
    public String offer() {
        return imageReference.offer();
    }

    @Override
    public String sku() {
        return imageReference.sku();
    }

    @Override
    public String version() {
        return imageReference.version();
    }

    @Override
    public ImageReference imageReference() {
        return imageReference;
    }

    @Override
    public PurchasePlan plan() {
        return innerModel().plan();
    }

    @Override
    public OSDiskImage osDiskImage() {
        return innerModel().osDiskImage();
    }

    @Override
    public Map<Integer, DataDiskImage> dataDiskImages() {
        if (innerModel().dataDiskImages() == null) {
            return Collections.unmodifiableMap(new HashMap<>());
        }
        HashMap<Integer, DataDiskImage> diskImages = new HashMap<>();
        for (DataDiskImage diskImage : innerModel().dataDiskImages()) {
            diskImages.put(diskImage.lun(), diskImage);
        }
        return Collections.unmodifiableMap(diskImages);
    }
}
