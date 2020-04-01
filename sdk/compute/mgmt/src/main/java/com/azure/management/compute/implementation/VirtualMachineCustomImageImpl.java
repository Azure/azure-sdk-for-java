/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.compute.models.ImageInner;
import com.azure.management.compute.CachingTypes;
import com.azure.management.compute.Disk;
import com.azure.management.compute.HyperVGenerationTypes;
import com.azure.management.compute.ImageDataDisk;
import com.azure.management.compute.ImageOSDisk;
import com.azure.management.compute.ImageStorageProfile;
import com.azure.management.compute.OperatingSystemStateTypes;
import com.azure.management.compute.OperatingSystemTypes;
import com.azure.management.compute.Snapshot;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineCustomImage;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation for VirtualMachineCustomImage.
 */
class VirtualMachineCustomImageImpl
        extends GroupableResourceImpl<
            VirtualMachineCustomImage,
            ImageInner,
            VirtualMachineCustomImageImpl,
            ComputeManager>
        implements
            VirtualMachineCustomImage,
            VirtualMachineCustomImage.Definition {

    VirtualMachineCustomImageImpl(final String name, ImageInner innerModel, final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
        //set the default value for the hyper-v generation
        this.inner().withHyperVGeneration(HyperVGenerationTypes.V1);
    }

    @Override
    public boolean isCreatedFromVirtualMachine() {
        return this.sourceVirtualMachineId() != null;
    }

    @Override
    public HyperVGenerationTypes hyperVGeneration() {
        return this.inner().hyperVGeneration();
    }

    @Override
    public String sourceVirtualMachineId() {
        if (this.inner().sourceVirtualMachine() == null) {
            return null;
        }
        return this.inner().sourceVirtualMachine().getId();
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
            return Collections.unmodifiableMap(new HashMap<>());
        }
        HashMap<Integer, ImageDataDisk> diskImages = new HashMap<>();
        for (ImageDataDisk dataDisk : this.inner().storageProfile().dataDisks()) {
            diskImages.put(dataDisk.lun(), dataDisk);
        }
        return Collections.unmodifiableMap(diskImages);
    }

    @Override
    public VirtualMachineCustomImageImpl fromVirtualMachine(String virtualMachineId) {
        this.inner().withSourceVirtualMachine(new SubResource().setId(virtualMachineId));
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
                .withSnapshot(new SubResource().setId(sourceSnapshotId));
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withLinuxFromSnapshot(String sourceSnapshotId, OperatingSystemStateTypes osState) {
        this.ensureOsDiskImage()
                .withOsState(osState)
                .withOsType(OperatingSystemTypes.LINUX)
                .withSnapshot(new SubResource().setId(sourceSnapshotId));

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
                .withManagedDisk(new SubResource().setId(sourceManagedDiskId));
        return this;
    }

    @Override
    public VirtualMachineCustomImageImpl withLinuxFromDisk(String sourceManagedDiskId, OperatingSystemStateTypes osState) {
        this.ensureOsDiskImage()
                .withOsState(osState)
                .withOsType(OperatingSystemTypes.LINUX)
                .withManagedDisk(new SubResource().setId(sourceManagedDiskId));
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
        return new CustomImageDataDiskImpl(new ImageDataDisk(), this);
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
    public VirtualMachineCustomImageImpl withZoneResilient() {
        this.ensureStorageProfile()
                .withZoneResilient(true);
        return this;
    }

    @Override
    public Mono<VirtualMachineCustomImage> createResourceAsync() {
        ensureDefaultLuns();
        return this.manager().inner().images().createOrUpdateAsync(resourceGroupName(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<ImageInner> getInnerAsync() {
        return this.manager().inner().images().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private ImageOSDisk ensureOsDiskImage() {
        this.ensureStorageProfile();
        if (this.inner().storageProfile().osDisk() == null) {
            this.inner()
                    .storageProfile()
                    .withOsDisk(new ImageOSDisk());
        }
        return this.inner().storageProfile().osDisk();
    }

    private ImageStorageProfile ensureStorageProfile() {
        if (this.inner().storageProfile() == null) {
            this.inner().withStorageProfile(new ImageStorageProfile());
        }
        return this.inner().storageProfile();
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

    @Override
    public VirtualMachineCustomImage.DefinitionStages.WithOSDiskImageSourceAltVirtualMachineSource withHyperVGeneration(HyperVGenerationTypes hyperVGeneration) {
        this.inner().withHyperVGeneration(hyperVGeneration);
        return this;
    }
}