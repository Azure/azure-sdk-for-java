// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AccessLevel;
import com.azure.resourcemanager.compute.models.CreationData;
import com.azure.resourcemanager.compute.models.CreationSource;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskCreateOption;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.DiskStorageAccountTypes;
import com.azure.resourcemanager.compute.models.GrantAccessData;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.SnapshotSku;
import com.azure.resourcemanager.compute.models.SnapshotSkuType;
import com.azure.resourcemanager.compute.models.SnapshotStorageAccountTypes;
import com.azure.resourcemanager.compute.fluent.inner.SnapshotInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.security.InvalidParameterException;

/** The implementation for Snapshot and its create and update interfaces. */
class SnapshotImpl extends GroupableResourceImpl<Snapshot, SnapshotInner, SnapshotImpl, ComputeManager>
    implements Snapshot, Snapshot.Definition, Snapshot.Update {

    private final ClientLogger logger = new ClientLogger(SnapshotImpl.class);

    SnapshotImpl(String name, SnapshotInner innerModel, final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
    }

    @Override
    public DiskSkuTypes sku() {
        if (this.inner().sku() == null || this.inner().sku().name() == null) {
            return null;
        } else {
            return DiskSkuTypes
                .fromStorageAccountType(DiskStorageAccountTypes.fromString(this.inner().sku().name().toString()));
        }
    }

    @Override
    public SnapshotSkuType skuType() {
        if (this.inner().sku() == null) {
            return null;
        } else {
            return SnapshotSkuType.fromSnapshotSku(this.inner().sku());
        }
    }

    @Override
    public DiskCreateOption creationMethod() {
        return this.inner().creationData().createOption();
    }

    @Override
    public boolean incremental() {
        return this.inner().incremental();
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
        return this.grantAccessAsync(accessDurationInSeconds).block();
    }

    @Override
    public Mono<String> grantAccessAsync(int accessDurationInSeconds) {
        GrantAccessData grantAccessDataInner = new GrantAccessData();
        grantAccessDataInner.withAccess(AccessLevel.READ).withDurationInSeconds(accessDurationInSeconds);
        return manager()
            .inner()
            .getSnapshots()
            .grantAccessAsync(resourceGroupName(), name(), grantAccessDataInner)
            .map(accessUriInner -> accessUriInner.accessSas());
    }

    @Override
    public void revokeAccess() {
        this.revokeAccessAsync().block();
    }

    @Override
    public Mono<Void> revokeAccessAsync() {
        return this.manager().inner().getSnapshots().revokeAccessAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public SnapshotImpl withLinuxFromVhd(String vhdUrl) {
        return withLinuxFromVhd(vhdUrl, constructStorageAccountId(vhdUrl));
    }

    @Override
    public SnapshotImpl withLinuxFromVhd(String vhdUrl, String storageAccountId) {
        this
            .inner()
            .withOsType(OperatingSystemTypes.LINUX)
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.IMPORT)
            .withSourceUri(vhdUrl)
            .withStorageAccountId(storageAccountId);
        return this;
    }

    @Override
    public SnapshotImpl withLinuxFromDisk(String sourceDiskId) {
        this
            .inner()
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
        this
            .inner()
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
        return withWindowsFromVhd(vhdUrl, constructStorageAccountId(vhdUrl));
    }

    @Override
    public SnapshotImpl withWindowsFromVhd(String vhdUrl, String storageAccountId) {
        this
            .inner()
            .withOsType(OperatingSystemTypes.WINDOWS)
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.IMPORT)
            .withSourceUri(vhdUrl)
            .withStorageAccountId(storageAccountId);
        return this;
    }

    @Override
    public SnapshotImpl withWindowsFromDisk(String sourceDiskId) {
        this
            .inner()
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
        this
            .inner()
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
        return withDataFromVhd(vhdUrl, constructStorageAccountId(vhdUrl));
    }

    @Override
    public SnapshotImpl withDataFromVhd(String vhdUrl, String storageAccountId) {
        this
            .inner()
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.IMPORT)
            .withSourceUri(vhdUrl)
            .withStorageAccountId(storageAccountId);
        return this;
    }

    @Override
    public SnapshotImpl withDataFromSnapshot(String snapshotId) {
        this
            .inner()
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
        this
            .inner()
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.COPY)
            .withSourceResourceId(managedDiskId);
        return this;
    }

    @Override
    public SnapshotImpl withDataFromDisk(Disk managedDisk) {
        return withDataFromDisk(managedDisk.id()).withOSType(managedDisk.osType()).withSku(managedDisk.sku());
    }

    @Override
    public SnapshotImpl withSizeInGB(int sizeInGB) {
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public SnapshotImpl withIncremental(boolean enabled) {
        this.inner().withIncremental(enabled);
        return this;
    }

    @Override
    public SnapshotImpl withOSType(OperatingSystemTypes osType) {
        this.inner().withOsType(osType);
        return this;
    }

    @Override
    public SnapshotImpl withSku(DiskSkuTypes sku) {
        SnapshotSku snapshotSku = new SnapshotSku();
        snapshotSku.withName(SnapshotStorageAccountTypes.fromString(sku.accountType().toString()));
        this.inner().withSku(snapshotSku);
        return this;
    }

    @Override
    public SnapshotImpl withSku(SnapshotSkuType sku) {
        this.inner().withSku(new SnapshotSku().withName(sku.accountType()));
        return this;
    }

    @Override
    public Mono<Snapshot> createResourceAsync() {
        return this
            .manager()
            .inner()
            .getSnapshots()
            .createOrUpdateAsync(resourceGroupName(), name(), this.inner())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<SnapshotInner> getInnerAsync() {
        return this.manager().inner().getSnapshots().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private String constructStorageAccountId(String vhdUrl) {
        try {
            return ResourceUtils.constructResourceId(this.manager().subscriptionId(),
                resourceGroupName(),
                "Microsoft.Storage",
                "storageAccounts",
                vhdUrl.split("\\.")[0].replace("https://", ""),
                "");
        } catch (RuntimeException ex) {
            throw logger.logExceptionAsError(
                new InvalidParameterException(String.format("%s is not valid URI of a blob to import.", vhdUrl)));
        }
    }
}
