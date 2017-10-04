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
import com.microsoft.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    DiskImpl(String name, DiskInner innerModel, final ComputeManager computeManager) {
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
    public boolean isAttachedToVirtualMachine() {
        return this.virtualMachineId() != null;
    }

    @Override
    public String virtualMachineId() {
        return this.inner().managedBy();
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
    public Set<AvailabilityZoneId> availabilityZones() {
        Set<AvailabilityZoneId> zones = new HashSet<>();
        if (this.inner().zones() != null) {
            for (String zone : this.inner().zones()) {
                zones.add(AvailabilityZoneId.fromString(zone));
            }
        }
        return Collections.unmodifiableSet(zones);
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

        return this.manager().inner().disks().grantAccessAsync(this.resourceGroupName(),
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
        return this.manager().inner().disks().revokeAccessAsync(this.resourceGroupName(), this.name()).toCompletable();
    }

    @Override
    public ServiceFuture<Void> revokeAccessAsync(ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.revokeAccessAsync(), callback);
    }

    @Override
    public DiskImpl withLinuxFromVhd(String vhdUrl) {
        this.inner()
                .withOsType(OperatingSystemTypes.LINUX)
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl withLinuxFromDisk(String sourceDiskId) {
        this.inner()
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
        this.inner()
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
        this.withSku(sourceSnapshot.sku());
        return this;
    }

    @Override
    public DiskImpl withWindowsFromVhd(String vhdUrl) {
        this.inner()
                .withOsType(OperatingSystemTypes.WINDOWS)
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl withWindowsFromDisk(String sourceDiskId) {
        this.inner()
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
        this.inner()
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
        this.withSku(sourceSnapshot.sku());
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
    public DiskImpl fromVhd(String vhdUrl) {
        this.inner()
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.IMPORT)
                .withSourceUri(vhdUrl);
        return this;
    }

    @Override
    public DiskImpl fromSnapshot(String snapshotId) {
        this.inner()
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
        this.inner()
                .withCreationData(new CreationData())
                .creationData()
                .withCreateOption(DiskCreateOption.COPY)
                .withSourceResourceId(managedDiskId);
        return this;
    }

    @Override
    public DiskImpl fromDisk(Disk managedDisk) {
        return fromDisk(managedDisk.id())
                .withOSType(managedDisk.osType())
                .withSku(managedDisk.sku());
    }

    @Override
    public DiskImpl withSizeInGB(int sizeInGB) {
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public DiskImpl withOSType(OperatingSystemTypes osType) {
        this.inner().withOsType(osType);
        return this;
    }

    @Override
    public DiskImpl withSku(DiskSkuTypes sku) {
        this.inner().withSku((new DiskSku()).withName(sku.accountType()));
        return this;
    }

    @Override
    public DiskImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        // Note: Zone is not updatable as of now, so this is available only during definition time.
        // Service return `ResourceAvailabilityZonesCannotBeModified` upon attempt to append a new
        // zone or remove one. Trying to remove the last one means attempt to change resource from
        // zonal to regional, which is not supported.
        if (this.inner().zones() == null) {
            this.inner().withZones(new ArrayList<String>());
        }
        this.inner().zones().add(zoneId.toString());
        return this;
    }

    @Override
    public Observable<Disk> createResourceAsync() {
        return manager().inner().disks().createOrUpdateAsync(resourceGroupName(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<DiskInner> getInnerAsync() {
        return this.manager().inner().disks().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }
}
