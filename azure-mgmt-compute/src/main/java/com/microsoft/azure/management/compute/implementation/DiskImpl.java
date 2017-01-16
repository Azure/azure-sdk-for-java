/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.AccessLevel;
import com.microsoft.azure.management.compute.CreationData;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.DiskCreateOption;
import com.microsoft.azure.management.compute.DiskSource;
import com.microsoft.azure.management.compute.ImageDiskReference;
import com.microsoft.azure.management.compute.OperatingSystemStateTypes;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

/**
 * The implementation for {@link Disk} and its create and update interfaces.
 */
@LangDefinition
class DiskImpl
        extends GroupableResourceImpl<
        Disk,
        DiskInner,
        DiskImpl,
        ComputeManager>
        implements
        Disk,
        Disk.Definition,
        Disk.Update  {
    private final DisksInner client;

    DiskImpl(String name,
             DiskInner innerModel,
             DisksInner client,
             final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
        this.client = client;
    }

    @Override
    public StorageAccountTypes accountType() {
        return this.inner().accountType();
    }

    @Override
    public DiskCreateOption createOption() {
        return this.inner().creationData().createOption();
    }

    @Override
    public boolean isAttachedToVirtualMachine() {
        return this.virtualMachineId() != null;
    }

    @Override
    public String virtualMachineId() {
        return this.inner().ownerId();
    }

    @Override
    public int sizeInGB() {
        return Utils.toPrimitiveInt(this.inner().diskSizeGB());
    }

    @Override
    public OperatingSystemStateTypes osState() {
        return this.inner().osState();
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.inner().osType();
    }

    @Override
    public DiskSource source() {
        return new DiskSource(this);
    }

    @Override
    public String grantAccess(AccessLevel accessLevel, int accessDuration) {
        GrantAccessDataInner grantAccessDataInner = new GrantAccessDataInner();
        grantAccessDataInner.withAccess(accessLevel)
                .withDurationInSeconds(accessDuration);

        AccessUriInner accessUriInner = this.client.grantAccess(this.resourceGroupName(),
                this.name(), grantAccessDataInner);
        if (accessUriInner == null) {
            return null;
        }
        return accessUriInner.accessSAS();
    }

    @Override
    public void revokeAccess() {
        this.client.revokeAccess(this.resourceGroupName(), this.name());
    }

    @Override
    public DiskImpl withOs() {
        this.inner()
                .withCreationData(new CreationData());
        return this;
    }

    @Override
    public DiskImpl withData() {
        this.inner()
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.EMPTY);
        return this;
    }

    @Override
    public DiskImpl fromImage(String imageId, OperatingSystemTypes osType, OperatingSystemStateTypes osState) {
        this.inner()
                .withOsType(osType)
                .withOsState(osState)
                .creationData()
                .withCreateOption(DiskCreateOption.FROM_IMAGE)
                .withImageReference(new ImageDiskReference().withId(imageId));
        return this;
    }

    @Override
    public DiskImpl fromImage(VirtualMachineImage image) {
        return this.fromImage(image.id(),
                image.osDiskImage().operatingSystem(),
                OperatingSystemStateTypes.GENERALIZED);
    }

    @Override
    public DiskImpl fromImage(VirtualMachineCustomImage image) {
        return this.fromImage(image.id(),
                image.osDiskImage().osType(),
                image.osDiskImage().osState());
    }

    @Override
    public DiskImpl importedFromSpecializedOsVhd(String vhdUrl, OperatingSystemTypes osType) {
        this.inner()
                .withOsType(osType)
                .withOsState(OperatingSystemStateTypes.SPECIALIZED)
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl importedFromGeneralizedOsVhd(String vhdUrl, OperatingSystemTypes osType) {
        this.inner()
                .withOsType(osType)
                .withOsState(OperatingSystemStateTypes.GENERALIZED)
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl fromImage(String imageId, int diskLun) {
        this.inner()
                .creationData()
                .withCreateOption(DiskCreateOption.FROM_IMAGE)
                .withImageReference(new ImageDiskReference().withId(imageId).withLun(diskLun));
        return this;
    }

    @Override
    public DiskImpl fromImage(VirtualMachineImage image, int diskLun) {
        if (!image.dataDiskImages().containsKey(diskLun)) {
            throw new IllegalArgumentException(String.format("A disk image with lun %d does not exists in the image", diskLun));
        }
        return fromImage(image.id(), diskLun);
    }

    @Override
    public DiskImpl fromImage(VirtualMachineCustomImage image, int diskLun) {
        if (!image.dataDiskImages().containsKey(diskLun)) {
            throw new IllegalArgumentException(String.format("A disk image with lun %d does not exists in the image", diskLun));
        }
        return fromImage(image.id(), diskLun);
    }

    @Override
    public DiskImpl importedFromDataVhd(String vhdUrl) {
        this.inner()
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl copiedFromSnapshot(String snapshotId) {
        this.inner()
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(snapshotId);
        return this;
    }

    @Override
    public DiskImpl copiedFromManagedDisk(String managedDiskId) {
        this.inner()
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(managedDiskId);
        return this;
    }

    @Override
    public DiskImpl copiedFromManagedDisk(Disk managedDisk) {
        return copiedFromManagedDisk(managedDisk.id())
                .withOsState(managedDisk.osState())
                .withOsType(managedDisk.osType())
                .withAccountType(managedDisk.accountType());
    }

    @Override
    public DiskImpl withSizeInGB(int sizeInGB) {
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public DiskImpl withOsType(OperatingSystemTypes osType) {
        this.inner().withOsType(osType);
        return this;
    }

    @Override
    public DiskImpl withOsState(OperatingSystemStateTypes osState) {
        this.inner().withOsState(osState);
        return this;
    }

    @Override
    public DiskImpl withAccountType(StorageAccountTypes accountType) {
        this.inner().withAccountType(accountType);
        return this;
    }

    @Override
    public Observable<Disk> createResourceAsync() {
        return client.createOrUpdateAsync(resourceGroupName(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Disk refresh() {
        DiskInner diskInner = this.client.get(this.resourceGroupName(), this.name());
        this.setInner(diskInner);
        return this;
    }
}
