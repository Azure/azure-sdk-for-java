/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.ImageDataDisk;
import com.microsoft.azure.management.compute.ImageOSDisk;
import com.microsoft.azure.management.compute.ImageStorageProfile;
import com.microsoft.azure.management.compute.OperatingSystemStateTypes;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.Snapshot;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation for {@link VirtualMachineCustomImage}.
 */
@LangDefinition
class VirtualMachineCustomImageImpl
        extends GroupableResourceImpl<
        VirtualMachineCustomImage,
        ImageInner,
        VirtualMachineCustomImageImpl,
        ComputeManager>
        implements
        VirtualMachineCustomImage,
        VirtualMachineCustomImage.Definition {

    private final ImagesInner client;

    VirtualMachineCustomImageImpl(final String name,
                                  ImageInner innerModel,
                                  final ImagesInner client,
                                  final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
        this.client = client;
    }

    @Override
    public boolean isCreatedFromVirtualMachine() {
        return this.sourceVirtualMachineId() != null;
    }

    @Override
    public String sourceVirtualMachineId() {
        if (this.inner().sourceVirtualMachine() == null) {
            return null;
        }
        return this.inner().sourceVirtualMachine().id();
    }

    @Override
    public ImageOSDisk osDiskImage() {
        if (this.inner().storageProfile() == null) {
            return null;
        }
        return this.inner().storageProfile().osDisk();
    }

    @Override
    public Map<Integer, ImageDataDisk> dataDiskImages() {
        if (this.inner().storageProfile() == null || this.inner().storageProfile().dataDisks() == null) {
            return Collections.unmodifiableMap(new HashMap<Integer, ImageDataDisk>());
        }
        HashMap<Integer, ImageDataDisk> diskImages = new HashMap<>();
        for (ImageDataDisk dataDisk : this.inner().storageProfile().dataDisks()) {
            diskImages.put(dataDisk.lun(), dataDisk);
        }
        return Collections.unmodifiableMap(diskImages);
    }

    @Override
    public VirtualMachineCustomImageImpl fromVirtualMachine(String virtualMachineId) {
        this.inner().withSourceVirtualMachine(new SubResource().withId(virtualMachineId));
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl fromVirtualMachine(VirtualMachine virtualMachine) {
        return this.fromVirtualMachine(virtualMachine.id());
    }


    @Override
    public VirtualMachineCustomImageImpl withWindowsFromVhd(String sourceVhdUrl, OperatingSystemStateTypes osState) {
        this.ensureOsDiskImage()
                .withOsState(osState)
                .withOsType(OperatingSystemTypes.WINDOWS)
                .withBlobUri(sourceVhdUrl);
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withLinuxFromVhd(String sourceVhdUrl, OperatingSystemStateTypes osState) {
        this.ensureOsDiskImage()
                .withOsState(osState)
                .withOsType(OperatingSystemTypes.LINUX)
                .withBlobUri(sourceVhdUrl);
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withWindowsFromSnapshot(String sourceSnapshotId, OperatingSystemStateTypes osState) {
        this.ensureOsDiskImage()
                .withOsState(osState)
                .withOsType(OperatingSystemTypes.WINDOWS)
                .withSnapshot(new SubResource().withId(sourceSnapshotId));
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withLinuxFromSnapshot(String sourceSnapshotId, OperatingSystemStateTypes osState) {
        this.ensureOsDiskImage()
                .withOsState(osState)
                .withOsType(OperatingSystemTypes.LINUX)
                .withSnapshot(new SubResource().withId(sourceSnapshotId));
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withWindowsFromSnapshot(Snapshot sourceSnapshot, OperatingSystemStateTypes osState) {
        return this.withWindowsFromSnapshot(sourceSnapshot.id(), osState);
    }

    @Override
    public VirtualMachineCustomImageImpl withLinuxFromSnapshot(Snapshot sourceSnapshot, OperatingSystemStateTypes osState) {
        return this.withLinuxFromSnapshot(sourceSnapshot.id(), osState);
    }

    @Override
    public VirtualMachineCustomImageImpl withWindowsFromDisk(String sourceManagedDiskId, OperatingSystemStateTypes osState) {
        this.ensureOsDiskImage()
                .withOsState(osState)
                .withOsType(OperatingSystemTypes.WINDOWS)
                .withManagedDisk(new SubResource().withId(sourceManagedDiskId));
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withLinuxFromDisk(String sourceManagedDiskId, OperatingSystemStateTypes osState) {
        this.ensureOsDiskImage()
                .withOsState(osState)
                .withOsType(OperatingSystemTypes.LINUX)
                .withManagedDisk(new SubResource().withId(sourceManagedDiskId));
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withWindowsFromDisk(Disk sourceManagedDisk, OperatingSystemStateTypes osState) {
        return withWindowsFromDisk(sourceManagedDisk.id(), osState);
    }

    @Override
    public VirtualMachineCustomImageImpl withLinuxFromDisk(Disk sourceManagedDisk, OperatingSystemStateTypes osState) {
        return withLinuxFromDisk(sourceManagedDisk.id(), osState);
    }

    @Override
    public VirtualMachineCustomImageImpl withDataDiskImageFromVhd(String sourceVhdUrl) {
        this.defineDataDiskImage()
                .withLun(-1)
                .fromVhd(sourceVhdUrl)
                .attach();
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withDataDiskImageFromSnapshot(String sourceSnapshotId) {
        this.defineDataDiskImage()
                .withLun(-1)
                .fromSnapshot(sourceSnapshotId)
                .attach();
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withDataDiskImageFromManagedDisk(String sourceManagedDiskId) {
        this.defineDataDiskImage()
                .withLun(-1)
                .fromManagedDisk(sourceManagedDiskId)
                .attach();
        return this;
    }

    @Override
    public CustomImageDataDiskImpl defineDataDiskImage() {
        return new CustomImageDataDiskImpl(new ImageDataDisk(),this);
    }

    @Override
    public VirtualMachineCustomImageImpl withOSDiskSizeInGB(int diskSizeGB) {
        this.ensureOsDiskImage()
                .withDiskSizeGB(diskSizeGB);
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withOSDiskCaching(CachingTypes cachingType) {
        this.ensureOsDiskImage()
                .withCaching(cachingType);
        return this;
    }

    @Override
    public Observable<VirtualMachineCustomImage> createResourceAsync() {
        ensureDefaultLuns();
        return client.createOrUpdateAsync(resourceGroupName(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public VirtualMachineCustomImage refresh() {
        ImageInner imageInner = this.client.get(this.resourceGroupName(), this.name());
        this.setInner(imageInner);
        return this;
    }

    private ImageOSDisk ensureOsDiskImage() {
        if (this.inner().storageProfile() == null) {
            this.inner().withStorageProfile(new ImageStorageProfile());
        }

        if (this.inner().storageProfile().osDisk() == null) {
            this.inner()
                    .storageProfile()
                    .withOsDisk(new ImageOSDisk());
        }
        return this.inner().storageProfile().osDisk();
    }

    private void ensureDefaultLuns() {
        if (this.inner().storageProfile() != null
                && this.inner().storageProfile().dataDisks() != null) {
            List<ImageDataDisk> imageDisks = this.inner().storageProfile().dataDisks();
            List<Integer> usedLuns = new ArrayList<>();
            for (ImageDataDisk imageDisk : imageDisks) {
                if (imageDisk.lun() != -1) {
                    usedLuns.add(imageDisk.lun());
                }
            }
            if (usedLuns.size() == imageDisks.size()) {
                return;
            }
            for (ImageDataDisk imageDisk : imageDisks) {
                if (imageDisk.lun() != -1) {
                    continue;
                }
                Integer i = 0;
                while (usedLuns.contains(i)) {
                    i++;
                }
                imageDisk.withLun(i);
                usedLuns.add(i);
            }
        }
    }

    VirtualMachineCustomImageImpl withCustomImageDataDisk(CustomImageDataDiskImpl customImageDataDisk) {
        if (this.inner().storageProfile() == null) {
            this.inner().withStorageProfile(new ImageStorageProfile());
        }
        if (this.inner().storageProfile().dataDisks() == null) {
            this.inner().storageProfile().withDataDisks(new ArrayList<ImageDataDisk>());
        }
        this.inner().storageProfile().dataDisks().add(customImageDataDisk.inner());
        return this;
    }
}