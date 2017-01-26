/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.SnapshotInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure managed snapshot.
 */
@Fluent
public interface Snapshot extends
        GroupableResource<ComputeManager>,
        Refreshable<Snapshot>,
        Wrapper<SnapshotInner>,
        Updatable<Snapshot.Update> {
    /**
     * @return the snapshot sku type
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
     * @return the readonly SAS uri to the snapshot
     */
    String grantAccess(int accessDurationInSeconds);

    /**
     * Revoke access granted to the snapshot.
     */
    void revokeAccess();

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
             * @param sourceDiskId source managed disk resource id
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withWindowsFromDisk(String sourceDiskId);

            /**
             * Specifies the source Windows OS managed disk.
             *
             * @param sourceDisk source managed disk
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withWindowsFromDisk(Disk sourceDisk);

            /**
             * Specifies the source Windows OS managed snapshot.
             *
             * @param sourceSnapshotId snapshot resource id
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withWindowsFromSnapshot(String sourceSnapshotId);

            /**
             * Specifies the source Windows OS managed snapshot.
             *
             * @param sourceSnapshot source snapshot
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withWindowsFromSnapshot(Snapshot sourceSnapshot);

            /**
             * Specifies the source specialized or generalized Windows OS vhd.
             *
             * @param vhdUrl the source vhd url
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withWindowsFromVhd(String vhdUrl);
        }

        /**
         *  The stage of the managed snapshot definition allowing to choose Linux OS source.
         */
        interface WithLinuxSnapshotSource {
            /**
             * Specifies the source Linux OS managed disk.
             *
             * @param sourceDiskId source managed disk resource id
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withLinuxFromDisk(String sourceDiskId);

            /**
             * Specifies the source Linux OS managed disk.
             *
             * @param sourceDisk source managed disk
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withLinuxFromDisk(Disk sourceDisk);

            /**
             * Specifies the source Linux OS managed snapshot.
             *
             * @param sourceSnapshotId snapshot resource id
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withLinuxFromSnapshot(String sourceSnapshotId);

            /**
             * Specifies the source Linux OS managed snapshot.
             *
             * @param sourceSnapshot source snapshot
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withLinuxFromSnapshot(Snapshot sourceSnapshot);

            /**
             * Specifies the source specialized or generalized Linux OS vhd.
             *
             * @param vhdUrl the source vhd url
             * @return the next stage of the managed snapshot definition
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
         * The stage of the managed disk definition allowing to choose source data disk vhd.
         */
        interface WithDataSnapshotFromVhd {
            /**
             * Specifies the source data vhd.
             *
             * @param vhdUrl the source vhd url
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withDataFromVhd(String vhdUrl);
        }

        /**
         * The stage of the managed disk definition allowing to choose managed disk containing data.
         */
        interface WithDataSnapshotFromDisk {
            /**
             * Specifies the id of source data managed disk.
             *
             * @param managedDiskId source managed disk resource id
             * @return the next stage of the managed disk definition
             */
            WithCreate withDataFromDisk(String managedDiskId);

            /**
             * Specifies the source data managed disk.
             *
             * @param managedDisk source managed disk
             * @return the next stage of the managed disk definition
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
             * @param snapshotId snapshot resource id
             * @return the next stage of the managed disk definition
             */
            WithCreate withDataFromSnapshot(String snapshotId);

            /**
             * Specifies the source data managed snapshot.
             *
             * @param snapshot snapshot resource
             * @return the next stage of the managed disk definition
             */
            WithCreate withDataFromSnapshot(Snapshot snapshot);
        }

        /**
         * The stage of the managed disk definition allowing to choose source operating system image.
         */
        interface WithOsSnapshotFromImage {
            /**
             * Specifies id of the image containing operating system.
             *
             * @param imageId image resource id
             * @param osType operating system type
             * @return the next stage of the managed disk definition
             */
            WithCreate fromImage(String imageId,
                                        OperatingSystemTypes osType);

            /**
             * Specifies the image containing operating system.
             *
             * @param image the image
             * @return the next stage of the managed disk definition
             */
            WithCreate fromImage(VirtualMachineImage image);

            /**
             * Specifies the custom image containing operating system.
             *
             * @param image the image
             * @return the next stage of the managed disk definition
             */
            WithCreate fromImage(VirtualMachineCustomImage image);
        }
        /**
         * The stage of the managed disk definition allowing to choose source data disk image.
         */
        interface WithDataSnapshotFromImage {
            /**
             * Specifies id of the image containing source data disk image.
             *
             * @param imageId image resource id
             * @param diskLun lun of the disk image
             * @return the next stage of the managed disk definition
             */
            WithCreate fromImage(String imageId,
                                        int diskLun);

            /**
             * Specifies the image containing source data disk image.
             *
             * @param image the image
             * @param diskLun lun of the disk image
             * @return the next stage of the managed disk definition
             */
            WithCreate fromImage(VirtualMachineImage image, int diskLun);

            /**
             * Specifies the custom image containing source data disk image.
             *
             * @param image the image
             * @param diskLun lun of the disk image
             * @return the next stage of the managed disk definition
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
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withSizeInGB(int sizeInGB);
        }

        /**
         * The stage of the managed disk definition allowing to choose account type.
         */
        interface WithSku {
            /**
             * Specifies the sku type.
             *
             * @param sku sku type
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withSku(DiskSkuTypes sku);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
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
             * @param sku sku type
             * @return the next stage of the managed snapshot update
             */
            Update withSku(DiskSkuTypes sku);
        }

        /**
         * The stage of the managed snapshot update allowing to specify Os settings.
         */
        interface WithOsSettings {
            /**
             * Specifies the operating system type.
             *
             * @param osType operating system type
             * @return the next stage of the managed snapshot update
             */
            Update withOSType(OperatingSystemTypes osType);
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Disk.Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<Snapshot>,
            Resource.UpdateWithTags<Snapshot.Update>,
            UpdateStages.WithSku,
            UpdateStages.WithOsSettings {
    }
}
