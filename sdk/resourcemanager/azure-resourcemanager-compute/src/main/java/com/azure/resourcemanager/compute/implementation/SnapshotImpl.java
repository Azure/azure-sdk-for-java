// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.models.SnapshotInner;
import com.azure.resourcemanager.compute.models.AccessLevel;
import com.azure.resourcemanager.compute.models.CopyCompletionError;
import com.azure.resourcemanager.compute.models.CreationData;
import com.azure.resourcemanager.compute.models.CreationSource;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskCreateOption;
import com.azure.resourcemanager.compute.models.GrantAccessData;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.PublicNetworkAccess;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.SnapshotSku;
import com.azure.resourcemanager.compute.models.SnapshotSkuType;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/** The implementation for Snapshot and its create and update interfaces. */
class SnapshotImpl extends GroupableResourceImpl<Snapshot, SnapshotInner, SnapshotImpl, ComputeManager>
    implements Snapshot, Snapshot.Definition, Snapshot.Update {

    private final ClientLogger logger = new ClientLogger(SnapshotImpl.class);

    SnapshotImpl(String name, SnapshotInner innerModel, final ComputeManager computeManager) {
        super(name, innerModel, computeManager);
    }

    @Override
    public SnapshotSkuType skuType() {
        if (this.innerModel().sku() == null) {
            return null;
        } else {
            return SnapshotSkuType.fromSnapshotSku(this.innerModel().sku());
        }
    }

    @Override
    public DiskCreateOption creationMethod() {
        return this.innerModel().creationData().createOption();
    }

    @Override
    public boolean incremental() {
        return this.innerModel().incremental();
    }

    @Override
    public int sizeInGB() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().diskSizeGB());
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
    public Float copyCompletionPercent() {
        return this.innerModel().completionPercent();
    }

    @Override
    public CopyCompletionError copyCompletionError() {
        return this.innerModel().copyCompletionError();
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
            .serviceClient()
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
        return this.manager().serviceClient().getSnapshots().revokeAccessAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public void awaitCopyStartCompletion() {
        awaitCopyStartCompletionAsync().block();
    }

    @Override
    public Boolean awaitCopyStartCompletion(Duration maxWaitTime) {
        Objects.requireNonNull(maxWaitTime);
        if (maxWaitTime.isNegative() || maxWaitTime.isZero()) {
            throw new IllegalArgumentException(String.format("Max wait time is non-positive: %dms", maxWaitTime.toMillis()));
        }
        return this.awaitCopyStartCompletionAsync()
            .then(Mono.just(Boolean.TRUE))
            .timeout(maxWaitTime, Mono.just(Boolean.FALSE))
            .block();
    }

    @Override
    public PublicNetworkAccess publicNetworkAccess() {
        return this.innerModel().publicNetworkAccess();
    }

    @Override
    public Mono<Void> awaitCopyStartCompletionAsync() {
        if (creationMethod() != DiskCreateOption.COPY_START) {
            return Mono.error(logger.logThrowableAsError(new IllegalStateException(
                String.format(
                    "\"awaitCopyStartCompletionAsync\" cannot be called on snapshot \"%s\" when \"creationMethod\" is not \"CopyStart\"", this.name()))));
        }

        return Flux.interval(Duration.ZERO, ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(manager().serviceClient().getDefaultPollInterval()))
            .flatMap(ignored -> getInnerAsync())
            .flatMap(inner -> {
                setInner(inner);
                Mono<SnapshotInner> result = Mono.just(inner);
                if (inner.copyCompletionError() != null) { // service error
                    result = Mono.error(new ManagementException(inner.copyCompletionError().errorMessage(), null));
                }
                return result;
            })
            .takeUntil(inner -> {
                if (Float.valueOf(100).equals(inner.completionPercent())) {
                    return true;
                } else { // in progress
                    logger.info("Wait for CopyStart complete for snapshot: {}. Complete percent: {}.",
                        inner.name(), inner.completionPercent());
                    return false;
                }
            })
            .then();
    }

    @Override
    public SnapshotImpl withLinuxFromVhd(String vhdUrl) {
        return withLinuxFromVhd(vhdUrl, constructStorageAccountId(vhdUrl));
    }

    @Override
    public SnapshotImpl withLinuxFromVhd(String vhdUrl, String storageAccountId) {
        this
            .innerModel()
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
            .innerModel()
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
        return this;
    }

    @Override
    public SnapshotImpl withLinuxFromSnapshot(String sourceSnapshotId) {
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
    public SnapshotImpl withLinuxFromSnapshot(Snapshot sourceSnapshot) {
        withLinuxFromSnapshot(sourceSnapshot.id());
        if (sourceSnapshot.osType() != null) {
            this.withOSType(sourceSnapshot.osType());
        }
        this.withSku(sourceSnapshot.skuType());
        return this;
    }

    @Override
    public SnapshotImpl withWindowsFromVhd(String vhdUrl) {
        return withWindowsFromVhd(vhdUrl, constructStorageAccountId(vhdUrl));
    }

    @Override
    public SnapshotImpl withWindowsFromVhd(String vhdUrl, String storageAccountId) {
        this
            .innerModel()
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
            .innerModel()
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
        return this;
    }

    @Override
    public SnapshotImpl withWindowsFromSnapshot(String sourceSnapshotId) {
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
    public SnapshotImpl withWindowsFromSnapshot(Snapshot sourceSnapshot) {
        withWindowsFromSnapshot(sourceSnapshot.id());
        if (sourceSnapshot.osType() != null) {
            this.withOSType(sourceSnapshot.osType());
        }
        this.withSku(sourceSnapshot.skuType());
        return this;
    }

    @Override
    public SnapshotImpl withDataFromVhd(String vhdUrl) {
        return withDataFromVhd(vhdUrl, constructStorageAccountId(vhdUrl));
    }

    @Override
    public SnapshotImpl withDataFromVhd(String vhdUrl, String storageAccountId) {
        this
            .innerModel()
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
            .innerModel()
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
    public SnapshotImpl withCopyStart() {
        this.innerModel()
            .creationData()
            .withCreateOption(DiskCreateOption.COPY_START);
        return this;
    }

    @Override
    public SnapshotImpl withDataFromDisk(String managedDiskId) {
        this
            .innerModel()
            .withCreationData(new CreationData())
            .creationData()
            .withCreateOption(DiskCreateOption.COPY)
            .withSourceResourceId(managedDiskId);
        return this;
    }

    @Override
    public SnapshotImpl withDataFromDisk(Disk managedDisk) {
        return withDataFromDisk(managedDisk.id()).withOSType(managedDisk.osType());
    }

    @Override
    public SnapshotImpl withSizeInGB(int sizeInGB) {
        this.innerModel().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public SnapshotImpl withIncremental(boolean enabled) {
        this.innerModel().withIncremental(enabled);
        return this;
    }

    @Override
    public SnapshotImpl withOSType(OperatingSystemTypes osType) {
        this.innerModel().withOsType(osType);
        return this;
    }

    @Override
    public SnapshotImpl withSku(SnapshotSkuType sku) {
        this.innerModel().withSku(new SnapshotSku().withName(sku.accountType()));
        return this;
    }

    @Override
    public Mono<Snapshot> createResourceAsync() {
        return this
            .manager()
            .serviceClient()
            .getSnapshots()
            .createOrUpdateAsync(resourceGroupName(), name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<SnapshotInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getSnapshots()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private String constructStorageAccountId(String vhdUrl) {
        try {
            return ResourceUtils
                .constructResourceId(
                    this.manager().subscriptionId(),
                    resourceGroupName(),
                    "Microsoft.Storage",
                    "storageAccounts",
                    vhdUrl.split("\\.")[0].replace("https://", ""),
                    "");
        } catch (RuntimeException ex) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(String.format("%s is not valid URI of a blob to import.", vhdUrl)));
        }
    }

    @Override
    public SnapshotImpl enablePublicNetworkAccess() {
        this.innerModel().withPublicNetworkAccess(PublicNetworkAccess.ENABLED);
        return this;
    }

    @Override
    public SnapshotImpl disablePublicNetworkAccess() {
        this.innerModel().withPublicNetworkAccess(PublicNetworkAccess.DISABLED);
        return this;
    }
}
