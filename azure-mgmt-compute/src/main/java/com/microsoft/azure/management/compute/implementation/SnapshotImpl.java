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
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.Snapshot;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

/**
 * The implementation for {@link Snapshot} and its create and update interfaces.
 */
@LangDefinition
class SnapshotImpl
        extends GroupableResourceImpl<
        Snapshot,
        SnapshotInner,
        SnapshotImpl,
        ComputeManager>
        implements
        Snapshot,
        Snapshot.Definition,
        Snapshot.Update  {
    private final SnapshotsInner client;

    SnapshotImpl(String name,
                 SnapshotInner innerModel,
                 SnapshotsInner client,
                 final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
        this.client = client;
    }

    @Override
    public StorageAccountTypes accountType() {
        return this.inner().accountType();
    }

    @Override
    public DiskCreateOption creationMethod() {
        return this.inner().creationData().createOption();
    }

    @Override
    public int sizeInGB() {
        return Utils.toPrimitiveInt(this.inner().diskSizeGB());
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.inner().osType();
    }

    @Override
    public DiskSource source() {
        // TODO
        return null;
        // return new DiskSource(this);
    }

    @Override
    public String grantAccess(int accessDurationInSeconds) {
        GrantAccessDataInner grantAccessDataInner = new GrantAccessDataInner();
        grantAccessDataInner
                .withDurationInSeconds(accessDurationInSeconds)
                .withAccess(AccessLevel.READ);

        AccessUriInner accessUriInner = this.client.grantAccess(this.resourceGroupName(),
                this.name(),
                grantAccessDataInner);
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
    public SnapshotImpl withOs() {
        this.inner()
                .withCreationData(new CreationData());
        return this;
    }

    @Override
    public SnapshotImpl withData() {
        this.inner()
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.EMPTY);
        return this;
    }

    @Override
    public SnapshotImpl importedFromOsVhd(String vhdUrl, OperatingSystemTypes osType) {
        this.inner()
                .withOsType(osType)
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public SnapshotImpl importedFromDataVhd(String vhdUrl) {
        this.inner()
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public SnapshotImpl copiedFromSnapshot(String snapshotId) {
        this.inner()
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(snapshotId);
        return this;
    }

    @Override
    public SnapshotImpl copiedFromManagedDisk(String managedDiskId) {
        this.inner()
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(managedDiskId);
        return this;
    }

    @Override
    public SnapshotImpl copiedFromManagedDisk(Disk managedDisk) {
        return copiedFromManagedDisk(managedDisk.id())
                .withOsType(managedDisk.osType())
                .withAccountType(managedDisk.accountType());
    }

    @Override
    public SnapshotImpl withSizeInGB(int sizeInGB) {
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public SnapshotImpl withOsType(OperatingSystemTypes osType) {
        this.inner().withOsType(osType);
        return this;
    }

    @Override
    public SnapshotImpl withAccountType(StorageAccountTypes accountType) {
        this.inner().withAccountType(accountType);
        return this;
    }

    @Override
    public Observable<Snapshot> createResourceAsync() {
        return client.createOrUpdateAsync(resourceGroupName(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Snapshot refresh() {
        SnapshotInner snapshotInner = this.client.get(this.resourceGroupName(), this.name());
        this.setInner(snapshotInner);
        return this;
    }

    public SnapshotImpl fromImage(VirtualMachineImage image) {
        return this.fromImage(image.id(),
                image.osDiskImage().operatingSystem());
    }

    public SnapshotImpl fromImage(VirtualMachineCustomImage image) {
        return this.fromImage(image.id(),
                image.osDiskImage().osType());
    }

    public SnapshotImpl fromImage(String imageId, OperatingSystemTypes osType) {
        this.inner()
                .withOsType(osType)
                .creationData()
                .withCreateOption(DiskCreateOption.FROM_IMAGE)
                .withImageReference(new ImageDiskReference().withId(imageId));
        return this;
    }

    public SnapshotImpl fromImage(String imageId, int diskLun) {
        this.inner()
                .creationData()
                .withCreateOption(DiskCreateOption.FROM_IMAGE)
                .withImageReference(new ImageDiskReference().withId(imageId).withLun(diskLun));
        return this;
    }

    public SnapshotImpl fromImage(VirtualMachineImage image, int diskLun) {
        if (!image.dataDiskImages().containsKey(diskLun)) {
            throw new IllegalArgumentException(String.format("A disk image with lun %d does not exists in the image", diskLun));
        }
        return fromImage(image.id(), diskLun);
    }

    public SnapshotImpl fromImage(VirtualMachineCustomImage image, int diskLun) {
        if (!image.dataDiskImages().containsKey(diskLun)) {
            throw new IllegalArgumentException(String.format("A disk image with lun %d does not exists in the image", diskLun));
        }
        return fromImage(image.id(), diskLun);
    }
}
