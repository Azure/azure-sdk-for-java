/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.SnapshotInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

/**
 * An immutable client-side representation of an Azure managed snapshot.
 */
@Fluent
public interface Snapshot extends
        GroupableResource<ComputeManager, SnapshotInner>,
        Refreshable<Snapshot>,
        Updatable<Snapshot.Update> {
    /**
     * @return the snapshot SKU type
     */
    DiskSkuTypes sku();

    /**
     * @return the snapshot creation method
     */
    DiskCreateOption creationMethod();

    /**
     * @return disk size in GB
     */
    int sizeInGB();

    /**
     * @return the type of operating system in the snapshot
     */
    OperatingSystemTypes osType();

    /**
     * @return the details of the source from which snapshot is created
     */
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
     * @return the observable to read-only SAS URI to the disk
     */
    @Beta
    Observable<String> grantAccessAsync(int accessDurationInSeconds);

    /**
     * Grants access to the snapshot asynchronously.
     *
     * @param accessDurationInSeconds the access duration in seconds
     * @param callback the callback to call on success or failure, on success it will pass read-only SAS URI to the disk in callback
     * @return a handle to cancel the request
     */
    @Beta
    ServiceFuture<String> grantAccessAsync(int accessDurationInSeconds, ServiceCallback<String> callback);

    /**
     * Revoke access granted to the snapshot.
     */
    void revokeAccess();

    /**
     * Revoke access granted to the snapshot asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    @Beta
    Completable revokeAccessAsync();

    /**
     * Revoke access granted to the snapshot asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    @Beta
    ServiceFuture<Void> revokeAccessAsync(ServiceCallback<Void> callback);

    /**
     * The entirety of the managed snapshot definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
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

    /**
     * Grouping of managed snapshot definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a managed snapshot definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the managed snapshot definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSnapshotSource> {
        }

        /**
         * The stage of the managed snapshot definition allowing to choose OS source or data source.
         */
        interface WithSnapshotSource
                extends
                WithWindowsSnapshotSource,
                WithLinuxSnapshotSource,
                WithDataSnapshotSource {
        }

        /**
         *  The stage of the managed snapshot definition allowing to choose Windows OS source.
         */
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
             * Specifies the source specialized or generalized Windows OS VHD.
             *
             * @param vhdUrl the source VHD URL
             * @return the next stage of the definition
             */
            WithCreate withWindowsFromVhd(String vhdUrl);
        }

        /**
         *  The stage of the managed snapshot definition allowing to choose a Linux OS source.
         */
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
             * Specifies the source specialized or generalized Linux OS VHD.
             *
             * @param vhdUrl the source VHD URL
             * @return the next stage of the definition
             */
            WithCreate withLinuxFromVhd(String vhdUrl);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose data source.
         */
        interface WithDataSnapshotSource extends
                WithDataSnapshotFromVhd,
                WithDataSnapshotFromDisk,
                WithDataSnapshotFromSnapshot {
        }

        /**
         * The stage of the managed disk definition allowing to choose source data disk VHD.
         */
        interface WithDataSnapshotFromVhd {
            /**
             * Specifies the source data VHD.
             *
             * @param vhdUrl a source VHD URL
             * @return the next stage of the definition
             */
            WithCreate withDataFromVhd(String vhdUrl);
        }

        /**
         * The stage of the managed disk definition allowing to choose managed disk containing data.
         */
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

        /**
         * The stage of the managed disk definition allowing to choose managed snapshot containing data.
         */
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

        /**
         * The stage of the managed disk definition allowing to choose a source operating system image.
         */
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
        /**
         * The stage of the managed disk definition allowing to choose source data disk image.
         */
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

        /**
         * The stage of the managed snapshot allowing to specify the size.
         */
        interface WithSize {
            /**
             * Specifies the disk size.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the definition
             */
            WithCreate withSizeInGB(int sizeInGB);
        }

        /**
         * The stage of the managed disk definition allowing to choose account type.
         */
        interface WithSku {
            /**
             * Specifies the SKU type.
             *
             * @param sku SKU type
             * @return the next stage of the definition
             */
            WithCreate withSku(DiskSkuTypes sku);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created, but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<Snapshot>,
                Resource.DefinitionWithTags<Snapshot.DefinitionStages.WithCreate>,
                WithSize,
                WithSku {
        }
    }

    /**
     * Grouping of managed snapshot update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the managed snapshot update allowing to choose account type.
         */
        interface WithSku {
            /**
             * Specifies the account type.
             *
             * @param sku SKU type
             * @return the next stage of the update
             */
            Update withSku(DiskSkuTypes sku);
        }

        /**
         * The stage of the managed snapshot update allowing to specify OS settings.
         */
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

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     */
    interface Update extends
            Appliable<Snapshot>,
            Resource.UpdateWithTags<Snapshot.Update>,
            UpdateStages.WithSku,
            UpdateStages.WithOSSettings {
    }
}
