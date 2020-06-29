// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.inner.SnapshotInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure managed snapshot. */
@Fluent
public interface Snapshot
    extends GroupableResource<ComputeManager, SnapshotInner>, Refreshable<Snapshot>, Updatable<Snapshot.Update> {
    /**
     * @return the snapshot SKU type.
     * @deprecated use {@link Snapshot#skuType()} instead.
     */
    @Deprecated
    DiskSkuTypes sku();

    /** @return the snapshot SKU type. */
    SnapshotSkuType skuType();

    /** @return whether a snapshot is incremental */
    boolean incremental();

    /** @return the snapshot creation method */
    DiskCreateOption creationMethod();

    /** @return disk size in GB */
    int sizeInGB();

    /** @return the type of operating system in the snapshot */
    OperatingSystemTypes osType();

    /** @return the details of the source from which snapshot is created */
    CreationSource source();

    /**
     * Grants access to the snapshot.
     *
     * @param accessDurationInSeconds the access duration in seconds
     * @return the read-only SAS URI to the snapshot
     */
    String grantAccess(int accessDurationInSeconds);

    /**
     * Grants access to the snapshot asynchronously.
     *
     * @param accessDurationInSeconds the access duration in seconds
     * @return a representation of the deferred computation of this call returning a read-only SAS URI to the disk
     */
    Mono<String> grantAccessAsync(int accessDurationInSeconds);

    /** Revoke access granted to the snapshot. */
    void revokeAccess();

    /**
     * Revoke access granted to the snapshot asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> revokeAccessAsync();

    /** The entirety of the managed snapshot definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSnapshotSource,
            DefinitionStages.WithWindowsSnapshotSource,
            DefinitionStages.WithLinuxSnapshotSource,
            DefinitionStages.WithDataSnapshotSource,
            DefinitionStages.WithDataSnapshotFromVhd,
            DefinitionStages.WithDataSnapshotFromDisk,
            DefinitionStages.WithDataSnapshotFromSnapshot,
            DefinitionStages.WithCreate {
    }

    /** Grouping of managed snapshot definition stages. */
    interface DefinitionStages {
        /** The first stage of a managed snapshot definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the managed snapshot definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSnapshotSource> {
        }

        /** The stage of the managed snapshot definition allowing to choose OS source or data source. */
        interface WithSnapshotSource
            extends WithWindowsSnapshotSource, WithLinuxSnapshotSource, WithDataSnapshotSource {
        }

        /** The stage of the managed snapshot definition allowing to choose Windows OS source. */
        interface WithWindowsSnapshotSource {
            /**
             * Specifies the source Windows OS managed disk.
             *
             * @param sourceDiskId a source managed disk resource ID
             * @return the next stage of the definition
             */
            WithCreate withWindowsFromDisk(String sourceDiskId);

            /**
             * Specifies the source Windows OS managed disk.
             *
             * @param sourceDisk a source managed disk
             * @return the next stage of the definition
             */
            WithCreate withWindowsFromDisk(Disk sourceDisk);

            /**
             * Specifies the source Windows OS managed snapshot.
             *
             * @param sourceSnapshotId a snapshot resource ID
             * @return the next stage of the definition
             */
            WithCreate withWindowsFromSnapshot(String sourceSnapshotId);

            /**
             * Specifies the source Windows OS managed snapshot.
             *
             * @param sourceSnapshot a source snapshot
             * @return the next stage of the definition
             */
            WithCreate withWindowsFromSnapshot(Snapshot sourceSnapshot);

            /**
             * Specifies the source specialized or generalized Windows OS VHD
             * when it belongs to the same subscription.
             *
             * @param vhdUrl the source VHD URL
             * @return the next stage of the definition
             */
            WithCreate withWindowsFromVhd(String vhdUrl);

            /**
             * Specifies the source specialized or generalized Windows OS VHD
             * and the storage account ID.
             *
             * @param vhdUrl the source VHD URL
             * @param storageAccountId the storage account ID
             * @return the next stage of the definition
             */
            WithCreate withWindowsFromVhd(String vhdUrl, String storageAccountId);
        }

        /** The stage of the managed snapshot definition allowing to choose a Linux OS source. */
        interface WithLinuxSnapshotSource {
            /**
             * Specifies the source Linux OS managed disk.
             *
             * @param sourceDiskId a source managed disk resource ID
             * @return the next stage of the definition
             */
            WithCreate withLinuxFromDisk(String sourceDiskId);

            /**
             * Specifies the source Linux OS managed disk.
             *
             * @param sourceDisk a source managed disk
             * @return the next stage of the definition
             */
            WithCreate withLinuxFromDisk(Disk sourceDisk);

            /**
             * Specifies the source Linux OS managed snapshot.
             *
             * @param sourceSnapshotId a snapshot resource ID
             * @return the next stage of the definition
             */
            WithCreate withLinuxFromSnapshot(String sourceSnapshotId);

            /**
             * Specifies the source Linux OS managed snapshot.
             *
             * @param sourceSnapshot a source snapshot
             * @return the next stage of the definition
             */
            WithCreate withLinuxFromSnapshot(Snapshot sourceSnapshot);

            /**
             * Specifies the source specialized or generalized Linux OS VHD
             * when it belongs to the same subscription.
             *
             * @param vhdUrl the source VHD URL
             * @return the next stage of the definition
             */
            WithCreate withLinuxFromVhd(String vhdUrl);

            /**
             * Specifies the source specialized or generalized Linux OS VHD
             * and the storage account ID.
             *
             * @param vhdUrl the source VHD URL
             * @param storageAccountId the storage account ID
             * @return the next stage of the definition
             */
            WithCreate withLinuxFromVhd(String vhdUrl, String storageAccountId);
        }

        /** The stage of the managed snapshot definition allowing to choose data source. */
        interface WithDataSnapshotSource
            extends WithDataSnapshotFromVhd, WithDataSnapshotFromDisk, WithDataSnapshotFromSnapshot {
        }

        /** The stage of the managed disk definition allowing to choose source data disk VHD. */
        interface WithDataSnapshotFromVhd {
            /**
             * Specifies the source data VHD when it belongs to the same subscription.
             *
             * @param vhdUrl a source VHD URL
             * @return the next stage of the definition
             */
            WithCreate withDataFromVhd(String vhdUrl);

            /**
             * Specifies the source data VHD and the storage account ID.
             *
             * @param vhdUrl a source VHD URL
             * @param storageAccountId the storage account ID
             * @return the next stage of the definition
             */
            WithCreate withDataFromVhd(String vhdUrl, String storageAccountId);
        }

        /** The stage of the managed disk definition allowing to choose managed disk containing data. */
        interface WithDataSnapshotFromDisk {
            /**
             * Specifies the ID of source data managed disk.
             *
             * @param managedDiskId source managed disk resource ID
             * @return the next stage of the definition
             */
            WithCreate withDataFromDisk(String managedDiskId);

            /**
             * Specifies the source data managed disk.
             *
             * @param managedDisk a source managed disk
             * @return the next stage of the definition
             */
            WithCreate withDataFromDisk(Disk managedDisk);
        }

        /** The stage of the managed disk definition allowing to choose managed snapshot containing data. */
        interface WithDataSnapshotFromSnapshot {
            /**
             * Specifies the source data managed snapshot.
             *
             * @param snapshotId a snapshot resource ID
             * @return the next stage of the definition
             */
            WithCreate withDataFromSnapshot(String snapshotId);

            /**
             * Specifies the source data managed snapshot.
             *
             * @param snapshot a snapshot resource
             * @return the next stage of the definition
             */
            WithCreate withDataFromSnapshot(Snapshot snapshot);
        }

        /** The stage of the managed disk definition allowing to choose a source operating system image. */
        interface WithOSSnapshotFromImage {
            /**
             * Specifies an image containing an operating system.
             *
             * @param imageId image resource ID
             * @param osType operating system type
             * @return the next stage of the definition
             */
            WithCreate fromImage(String imageId, OperatingSystemTypes osType);

            /**
             * Specifies an image containing an operating system.
             *
             * @param image the image
             * @return the next stage of the definition
             */
            WithCreate fromImage(VirtualMachineImage image);

            /**
             * Specifies a custom image containing an operating system.
             *
             * @param image the image
             * @return the next stage of the definition
             */
            WithCreate fromImage(VirtualMachineCustomImage image);
        }
        /** The stage of the managed disk definition allowing to choose source data disk image. */
        interface WithDataSnapshotFromImage {
            /**
             * Specifies an image containing source data disk image.
             *
             * @param imageId an image resource ID
             * @param diskLun LUN of the disk image
             * @return the next stage of the definition
             */
            WithCreate fromImage(String imageId, int diskLun);

            /**
             * Specifies an image containing a source data disk image.
             *
             * @param image an image
             * @param diskLun LUN of the disk image
             * @return the next stage of the definition
             */
            WithCreate fromImage(VirtualMachineImage image, int diskLun);

            /**
             * Specifies a custom image containing a source data disk image.
             *
             * @param image the image
             * @param diskLun LUN of the disk image
             * @return the next stage of the definition
             */
            WithCreate fromImage(VirtualMachineCustomImage image, int diskLun);
        }

        /** The stage of the managed snapshot allowing to specify the size. */
        interface WithSize {
            /**
             * Specifies the disk size.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the definition
             */
            WithCreate withSizeInGB(int sizeInGB);
        }

        /** The stage of the managed snapshot allowing to specify incremental snapshot. */
        interface WithIncremental {
            /**
             * Specifies whether a snapshot is incremental.
             *
             * @param enabled whether to enable incremental snapshot
             * @return the next stage of the definition
             */
            WithCreate withIncremental(boolean enabled);
        }

        /** The stage of the snapshot definition allowing to choose account type. */
        interface WithSku {
            /**
             * Specifies the SKU type.
             *
             * @deprecated use {@link WithSku#withSku(SnapshotSkuType)} instead.
             * @param sku SKU type
             * @return the next stage of the definition
             */
            @Deprecated
            WithCreate withSku(DiskSkuTypes sku);

            /**
             * Specifies the SKU type.
             *
             * @param sku SKU type
             * @return the next stage of the definition
             */
            WithCreate withSku(SnapshotSkuType sku);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<Snapshot>,
                Resource.DefinitionWithTags<Snapshot.DefinitionStages.WithCreate>,
                WithSize,
                WithSku,
                WithIncremental {
        }
    }

    /** Grouping of managed snapshot update stages. */
    interface UpdateStages {
        /** The stage of the managed snapshot update allowing to choose account type. */
        interface WithSku {
            /**
             * Specifies the SKU type.
             *
             * @deprecated use {@link WithSku#withSku(SnapshotSkuType)} instead.
             * @param sku SKU type
             * @return the next stage of the update
             */
            @Deprecated
            Update withSku(DiskSkuTypes sku);

            /**
             * Specifies the SKU type.
             *
             * @param sku SKU type
             * @return the next stage of the update
             */
            Update withSku(SnapshotSkuType sku);
        }

        /** The stage of the managed snapshot update allowing to specify OS settings. */
        interface WithOSSettings {
            /**
             * Specifies the operating system type.
             *
             * @param osType operating system type
             * @return the next stage of the update
             */
            Update withOSType(OperatingSystemTypes osType);
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<Snapshot>,
            Resource.UpdateWithTags<Snapshot.Update>,
            UpdateStages.WithSku,
            UpdateStages.WithOSSettings {
    }
}
