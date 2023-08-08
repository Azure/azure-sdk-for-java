// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AccessLevel;
import com.azure.resourcemanager.compute.models.CreationData;
import com.azure.resourcemanager.compute.models.CreationSource;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskCreateOption;
import com.azure.resourcemanager.compute.models.DiskSku;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.DiskStorageAccountTypes;
import com.azure.resourcemanager.compute.models.EncryptionSettingsCollection;
import com.azure.resourcemanager.compute.models.GrantAccessData;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.fluent.models.DiskInner;
import com.azure.resourcemanager.compute.models.SnapshotSkuType;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** The implementation for {@link Disk} and its create and update interfaces. */
class DiskImpl extends GroupableResourceImpl<Disk, DiskInner, DiskImpl, ComputeManager>
    implements Disk, Disk.Definition, Disk.Update {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    DiskImpl(String name, DiskInner innerModel, final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
    }

    @Override
    public DiskSkuTypes sku() {
        return DiskSkuTypes.fromDiskSku(this.innerModel().sku());
    }

    @Override
    public DiskCreateOption creationMethod() {
        return this.innerModel().creationData().createOption();
    }

    @Override
    public boolean isAttachedToVirtualMachine() {
        return this.virtualMachineId() != null;
    }

    @Override
    public String virtualMachineId() {
        return this.innerModel().managedBy();
    }

    @Override
    public int sizeInGB() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().diskSizeGB());
    }

    @Override
    public long sizeInByte() {
        return this.innerModel().diskSizeBytes();
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.innerModel().osType();
    }

    @Override
    public CreationSource source() {
        return new CreationSource(this.innerModel().creationData());
    }

    @Override
    public Set<AvailabilityZoneId> availabilityZones() {
        Set<AvailabilityZoneId> zones = new HashSet<>();
        if (this.innerModel().zones() != null) {
            for (String zone : this.innerModel().zones()) {
                zones.add(AvailabilityZoneId.fromString(zone));
            }
        }
        return Collections.unmodifiableSet(zones);
    }

    @Override
    public EncryptionSettingsCollection encryptionSettings() {
        return this.innerModel().encryptionSettingsCollection();
    }

    @Override
    public String grantAccess(int accessDurationInSeconds) {
        return this.grantAccessAsync(accessDurationInSeconds).block();
    }

    @Override
    public Mono<String> grantAccessAsync(int accessDurationInSeconds) {
        GrantAccessData grantAccessDataInner = new GrantAccessData();
        grantAccessDataInner.withAccess(AccessLevel.READ).withDurationInSeconds(accessDurationInSeconds);

        return this
            .manager()
            .serviceClient()
            .getDisks()
            .grantAccessAsync(this.resourceGroupName(), this.name(), grantAccessDataInner)
            .map(accessUriInner -> accessUriInner.accessSas());
    }

    @Override
    public void revokeAccess() {
        this.revokeAccessAsync().block();
    }

    @Override
    public Mono<Void> revokeAccessAsync() {
        return this.manager().serviceClient().getDisks().revokeAccessAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public DiskImpl withLinuxFromVhd(String vhdUrl) {
        this
            .innerModel()
            .withOsType(OperatingSystemTypes.LINUX)
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.IMPORT)
            .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl withLinuxFromDisk(String sourceDiskId) {
        this
            .innerModel()
            .withOsType(OperatingSystemTypes.LINUX)
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.COPY)
            .withSourceResourceId(sourceDiskId);
        return this;
    }

    @Override
    public DiskImpl withLinuxFromDisk(Disk sourceDisk) {
        withLinuxFromDisk(sourceDisk.id());
        if (sourceDisk.osType() != null) {
            this.withOSType(sourceDisk.osType());
        }
        this.withSku(sourceDisk.sku());
        return this;
    }

    @Override
    public DiskImpl withLinuxFromSnapshot(String sourceSnapshotId) {
        this
            .innerModel()
            .withOsType(OperatingSystemTypes.LINUX)
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.COPY)
            .withSourceResourceId(sourceSnapshotId);
        return this;
    }

    @Override
    public DiskImpl withLinuxFromSnapshot(Snapshot sourceSnapshot) {
        withLinuxFromSnapshot(sourceSnapshot.id());
        if (sourceSnapshot.osType() != null) {
            this.withOSType(sourceSnapshot.osType());
        }
        this.withSku(this.fromSnapshotSkuType(sourceSnapshot.skuType()));
        return this;
    }

    @Override
    public DiskImpl withWindowsFromVhd(String vhdUrl) {
        this
            .innerModel()
            .withOsType(OperatingSystemTypes.WINDOWS)
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.IMPORT)
            .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl withWindowsFromDisk(String sourceDiskId) {
        this
            .innerModel()
            .withOsType(OperatingSystemTypes.WINDOWS)
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.COPY)
            .withSourceResourceId(sourceDiskId);
        return this;
    }

    @Override
    public DiskImpl withWindowsFromDisk(Disk sourceDisk) {
        withWindowsFromDisk(sourceDisk.id());
        if (sourceDisk.osType() != null) {
            this.withOSType(sourceDisk.osType());
        }
        this.withSku(sourceDisk.sku());
        return this;
    }

    @Override
    public DiskImpl withWindowsFromSnapshot(String sourceSnapshotId) {
        this
            .innerModel()
            .withOsType(OperatingSystemTypes.WINDOWS)
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.COPY)
            .withSourceResourceId(sourceSnapshotId);
        return this;
    }

    @Override
    public DiskImpl withWindowsFromSnapshot(Snapshot sourceSnapshot) {
        withWindowsFromSnapshot(sourceSnapshot.id());
        if (sourceSnapshot.osType() != null) {
            this.withOSType(sourceSnapshot.osType());
        }
        this.withSku(this.fromSnapshotSkuType(sourceSnapshot.skuType()));
        return this;
    }

    @Override
    public DiskImpl withData() {
        this.innerModel().withCreationData(new CreationData()).creationData().withCreateOption(DiskCreateOption.EMPTY);
        return this;
    }

    @Override
    public DiskImpl fromVhd(String vhdUrl) {
        this
            .innerModel()
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.IMPORT)
            .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl withUploadSizeInMB(long uploadSizeInMB) {
        this
            .innerModel()
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.UPLOAD)
            .withUploadSizeBytes(uploadSizeInMB * 1024 * 1024 + 512);
        return this;
    }

    @Override
    public DiskImpl fromSnapshot(String snapshotId) {
        this
            .innerModel()
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.COPY)
            .withSourceResourceId(snapshotId);
        return this;
    }

    @Override
    public DiskImpl fromSnapshot(Snapshot snapshot) {
        return fromSnapshot(snapshot.id());
    }

    @Override
    public DiskImpl fromDisk(String managedDiskId) {
        this
            .innerModel()
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.COPY)
            .withSourceResourceId(managedDiskId);
        return this;
    }

    @Override
    public DiskImpl fromDisk(Disk managedDisk) {
        return fromDisk(managedDisk.id()).withOSType(managedDisk.osType()).withSku(managedDisk.sku());
    }

    @Override
    public DiskImpl withSizeInGB(int sizeInGB) {
        this.innerModel().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public DiskImpl withOSType(OperatingSystemTypes osType) {
        this.innerModel().withOsType(osType);
        return this;
    }

    @Override
    public DiskImpl withSku(DiskSkuTypes sku) {
        this.innerModel().withSku((new DiskSku()).withName(sku.accountType()));
        return this;
    }

    @Override
    public DiskImpl withStorageAccountId(String storageAccountId) {
        this.innerModel().creationData().withStorageAccountId(storageAccountId);
        return this;
    }

    @Override
    public DiskImpl withStorageAccountName(String storageAccountName) {
        String id =
            ResourceUtils
                .constructResourceId(
                    this.myManager.subscriptionId(),
                    this.resourceGroupName(),
                    "Microsoft.Storage",
                    "storageAccounts",
                    storageAccountName,
                    "");
        return this.withStorageAccountId(id);
    }

    @Override
    public DiskImpl withStorageAccount(StorageAccount account) {
        return this.withStorageAccountId(account.id());
    }

    @Override
    public DiskImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        // Note: Zone is not updatable as of now, so this is available only during definition time.
        // Service return `ResourceAvailabilityZonesCannotBeModified` upon attempt to append a new
        // zone or remove one. Trying to remove the last one means attempt to change resource from
        // zonal to regional, which is not supported.
        if (this.innerModel().zones() == null) {
            this.innerModel().withZones(new ArrayList<String>());
        }
        this.innerModel().zones().add(zoneId.toString());
        return this;
    }

    @Override
    public Mono<Disk> createResourceAsync() {
        return manager()
            .serviceClient()
            .getDisks()
            .createOrUpdateAsync(resourceGroupName(), name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<DiskInner> getInnerAsync() {
        return this.manager().serviceClient().getDisks().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Accepted<Disk> beginCreate() {
        return AcceptedImpl
            .newAccepted(
                logger,
                this.manager().serviceClient().getHttpPipeline(),
                this.manager().serviceClient().getDefaultPollInterval(),
                () ->
                    this
                        .manager()
                        .serviceClient()
                        .getDisks()
                        .createOrUpdateWithResponseAsync(resourceGroupName(), name(), this.innerModel())
                        .block(),
                inner -> new DiskImpl(inner.name(), inner, this.manager()),
                DiskInner.class,
                () -> {
                    Flux<Indexable> dependencyTasksAsync =
                        taskGroup().invokeDependencyAsync(taskGroup().newInvocationContext());
                    dependencyTasksAsync.blockLast();
                },
                this::setInner,
                Context.NONE);
    }

    private DiskSkuTypes fromSnapshotSkuType(SnapshotSkuType skuType) {
        if (skuType == null) {
            return null;
        }
        return DiskSkuTypes.fromStorageAccountType(DiskStorageAccountTypes.fromString(skuType.toString()));
    }
}
