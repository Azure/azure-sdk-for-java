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
import com.microsoft.azure.management.compute.CreationSource;
import com.microsoft.azure.management.compute.DiskSku;
import com.microsoft.azure.management.compute.DiskSkuTypes;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.Snapshot;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for Snapshot and its create and update interfaces.
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

    SnapshotImpl(String name,
             SnapshotInner innerModel,
             final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
    }

    @Override
    public DiskSkuTypes sku() {
        return DiskSkuTypes.fromDiskSku(this.inner().sku());
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
    public CreationSource source() {
        return new CreationSource(this.inner().creationData());
    }

    @Override
    public String grantAccess(int accessDurationInSeconds) {
        return this.grantAccessAsync(accessDurationInSeconds).toBlocking().last();
    }

    @Override
    public Observable<String> grantAccessAsync(int accessDurationInSeconds) {
        GrantAccessDataInner grantAccessDataInner = new GrantAccessDataInner();
        grantAccessDataInner.withAccess(AccessLevel.READ)
                .withDurationInSeconds(accessDurationInSeconds);

        return this.manager().inner().snapshots().grantAccessAsync(this.resourceGroupName(),
                this.name(), grantAccessDataInner).map(new Func1<AccessUriInner, String>() {
            @Override
            public String call(AccessUriInner accessUriInner) {
                if (accessUriInner == null) {
                    return null;
                }
                return accessUriInner.accessSAS();
            }
        });
    }

    @Override
    public ServiceFuture<String> grantAccessAsync(int accessDurationInSeconds, ServiceCallback<String> callback) {
        return ServiceFuture.fromBody(this.grantAccessAsync(accessDurationInSeconds), callback);
    }

    @Override
    public void revokeAccess() {
        this.revokeAccessAsync().await();
    }

    @Override
    public Completable revokeAccessAsync() {
        return this.manager().inner().snapshots().revokeAccessAsync(this.resourceGroupName(), this.name()).toCompletable();
    }

    @Override
    public ServiceFuture<Void> revokeAccessAsync(ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.revokeAccessAsync(), callback);
    }

    @Override
    public SnapshotImpl withLinuxFromVhd(String vhdUrl) {
        this.inner()
                .withOsType(OperatingSystemTypes.LINUX)
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public SnapshotImpl withLinuxFromDisk(String sourceDiskId) {
        this.inner()
                .withOsType(OperatingSystemTypes.LINUX)
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(sourceDiskId);
        return this;
    }

    @Override
    public SnapshotImpl withLinuxFromDisk(Disk sourceDisk) {
        withLinuxFromDisk(sourceDisk.id());
        if (sourceDisk.osType() != null) {
            this.withOSType(sourceDisk.osType());
        }
        this.withSku(sourceDisk.sku());
        return this;
    }

    @Override
    public SnapshotImpl withLinuxFromSnapshot(String sourceSnapshotId) {
        this.inner()
                .withOsType(OperatingSystemTypes.LINUX)
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(sourceSnapshotId);
        return this;
    }

    @Override
    public SnapshotImpl withLinuxFromSnapshot(Snapshot sourceSnapshot) {
        withLinuxFromSnapshot(sourceSnapshot.id());
        if (sourceSnapshot.osType() != null) {
            this.withOSType(sourceSnapshot.osType());
        }
        this.withSku(sourceSnapshot.sku());
        return this;
    }

    @Override
    public SnapshotImpl withWindowsFromVhd(String vhdUrl) {
        this.inner()
                .withOsType(OperatingSystemTypes.WINDOWS)
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public SnapshotImpl withWindowsFromDisk(String sourceDiskId) {
        this.inner()
                .withOsType(OperatingSystemTypes.WINDOWS)
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(sourceDiskId);
        return this;
    }

    @Override
    public SnapshotImpl withWindowsFromDisk(Disk sourceDisk) {
        withWindowsFromDisk(sourceDisk.id());
        if (sourceDisk.osType() != null) {
            this.withOSType(sourceDisk.osType());
        }
        this.withSku(sourceDisk.sku());
        return this;
    }

    @Override
    public SnapshotImpl withWindowsFromSnapshot(String sourceSnapshotId) {
        this.inner()
                .withOsType(OperatingSystemTypes.WINDOWS)
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(sourceSnapshotId);
        return this;
    }

    @Override
    public SnapshotImpl withWindowsFromSnapshot(Snapshot sourceSnapshot) {
        withWindowsFromSnapshot(sourceSnapshot.id());
        if (sourceSnapshot.osType() != null) {
            this.withOSType(sourceSnapshot.osType());
        }
        this.withSku(sourceSnapshot.sku());
        return this;
    }

    @Override
    public SnapshotImpl withDataFromVhd(String vhdUrl) {
        this.inner()
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public SnapshotImpl withDataFromSnapshot(String snapshotId) {
        this.inner()
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(snapshotId);
        return this;
    }

    @Override
    public SnapshotImpl withDataFromSnapshot(Snapshot snapshot) {
        return withDataFromSnapshot(snapshot.id());
    }

    @Override
    public SnapshotImpl withDataFromDisk(String managedDiskId) {
        this.inner()
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(managedDiskId);
        return this;
    }

    @Override
    public SnapshotImpl withDataFromDisk(Disk managedDisk) {
        return withDataFromDisk(managedDisk.id())
                .withOSType(managedDisk.osType())
                .withSku(managedDisk.sku());
    }

    @Override
    public SnapshotImpl withSizeInGB(int sizeInGB) {
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public SnapshotImpl withOSType(OperatingSystemTypes osType) {
        this.inner().withOsType(osType);
        return this;
    }

    @Override
    public SnapshotImpl withSku(DiskSkuTypes sku) {
        this.inner().withSku((new DiskSku()).withName(sku.accountType()));
        return this;
    }

    @Override
    public Observable<Snapshot> createResourceAsync() {
        return this.manager().inner().snapshots().createOrUpdateAsync(resourceGroupName(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<SnapshotInner> getInnerAsync() {
        return this.manager().inner().snapshots().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }
}
