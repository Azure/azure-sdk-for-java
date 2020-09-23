// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.ImageDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/** The implementation for {@link VirtualMachineCustomImage.CustomImageDataDisk}. */
class CustomImageDataDiskImpl
    extends ChildResourceImpl<ImageDataDisk, VirtualMachineCustomImageImpl, VirtualMachineCustomImage>
    implements VirtualMachineCustomImage.CustomImageDataDisk,
        VirtualMachineCustomImage.CustomImageDataDisk.Definition<
            VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOSDiskSettings> {

    CustomImageDataDiskImpl(ImageDataDisk inner, VirtualMachineCustomImageImpl parent) {
        super(inner, parent);
    }

    @Override
    public CustomImageDataDiskImpl withDiskSizeInGB(int diskSizeGB) {
        this.innerModel().withDiskSizeGB(diskSizeGB);
        return this;
    }

    @Override
    public CustomImageDataDiskImpl withDiskCaching(CachingTypes cachingType) {
        this.innerModel().withCaching(cachingType);
        return this;
    }

    @Override
    public CustomImageDataDiskImpl withLun(int lun) {
        this.innerModel().withLun(lun);
        return this;
    }

    @Override
    public CustomImageDataDiskImpl fromVhd(String sourceVhdUrl) {
        this.innerModel().withBlobUri(sourceVhdUrl);
        return this;
    }

    @Override
    public CustomImageDataDiskImpl fromSnapshot(String sourceSnapshotId) {
        this.innerModel().withSnapshot(new SubResource().withId(sourceSnapshotId));
        return this;
    }

    @Override
    public CustomImageDataDiskImpl fromManagedDisk(String sourceManagedDiskId) {
        this.innerModel().withManagedDisk(new SubResource().withId(sourceManagedDiskId));
        return this;
    }

    @Override
    public CustomImageDataDiskImpl fromManagedDisk(Disk sourceManagedDisk) {
        this.innerModel().withManagedDisk(new SubResource().withId(sourceManagedDisk.id()));
        return this;
    }

    @Override
    public String name() {
        return Integer.toString(this.innerModel().lun());
    }

    @Override
    public VirtualMachineCustomImageImpl attach() {
        return this.parent().withCustomImageDataDisk(this);
    }
}
