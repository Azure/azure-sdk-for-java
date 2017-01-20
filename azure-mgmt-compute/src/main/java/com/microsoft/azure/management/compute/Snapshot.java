/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
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
        GroupableResource,
        Refreshable<Snapshot>,
        Wrapper<SnapshotInner>,
        Updatable<Snapshot.Update> {
    /**
     * @return the snapshot storage account type
     */
    StorageAccountTypes accountType();

    /**
     * @return the snapshot creation method
     */
    DiskCreateOption creationMethod();

    /**
     * @return snapshot size in GB
     */
    int sizeInGB();

    /**
     * @return the type of operating system in the snapshot
     */
    OperatingSystemTypes osType();

    /**
     * @return the details of the source from which snapshot is created
     */
    DiskSource source();

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
            DefinitionStages.WithSnapshotType,
            DefinitionStages.WithOsSnapshot,
            DefinitionStages.WithDataSnapshot,
            DefinitionStages.WithCreateAndOsSettings {
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
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSnapshotType> {
        }

        /**
         * The stage of the managed snapshot definition allowing to choose snapshot type.
         */
        interface WithSnapshotType {
            /**
             * Begins definition of managed snapshot containing operating system.
             *
             * @return the next stage of the managed snapshot definition
             */
            WithOsSnapshot withOs();

            /**
             * Begins definition of managed snapshot containing data.
             *
             * @return the next stage of the managed snapshot definition
             */
            WithDataSnapshot withData();
        }

        /**
         * The stage of the managed snapshot definition allowing to choose Os source.
         */
        interface WithOsSnapshot extends
                WithOsSnapshotFromVhd,
                WithOsSnapshotFromManagedDisk,
                WithOsSnapshotFromSnapshot {
        }

        /**
         * The stage of the managed snapshot definition allowing to choose data source.
         */
        interface WithDataSnapshot extends
                WithDataSnapshotFromVhd,
                WithDataSnapshotFromManagedDisk,
                WithDataSnapshotFromSnapshot {
        }

        /**
         * The stage of the managed snapshot definition allowing to choose source vhd containing Os.
         */
        interface WithOsSnapshotFromVhd {
            /**
             * Specifies the source specialized or generalized operating system vhd.
             *
             * @param vhdUrl the source vhd url
             * @param osType operating system type
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize importedFromOsVhd(String vhdUrl, OperatingSystemTypes osType);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose managed disk containing os.
         */
        interface WithOsSnapshotFromManagedDisk {
            /**
             * Specifies the source operating system managed disk.
             *
             * @param managedDiskId source managed disk resource id
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndOsSettings copiedFromManagedDisk(String managedDiskId);

            /**
             * Specifies the source operating system managed disk.
             *
             * @param managedDisk source managed disk
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndOsSettings copiedFromManagedDisk(Disk managedDisk);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose managed snapshot containing os.
         */
        interface WithOsSnapshotFromSnapshot {
            /**
             * Specifies the source operating system managed snapshot.
             *
             * @param snapshotId snapshot resource id
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndOsSettings copiedFromSnapshot(String snapshotId);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose source operating system image.
         */
        interface WithOsDSnapshotFromImage {
            /**
             * Specifies id of the image containing operating system.
             *
             * @param imageId image resource id
             * @param osType operating system type
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize fromImage(String imageId,
                                        OperatingSystemTypes osType);

            /**
             * Specifies the image containing operating system.
             *
             * @param image the image
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize fromImage(VirtualMachineImage image);

            /**
             * Specifies the custom image containing operating system.
             *
             * @param image the image
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize fromImage(VirtualMachineCustomImage image);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose source data disk vhd.
         */
        interface WithDataSnapshotFromVhd {
            /**
             * Specifies the source data vhd.
             *
             * @param vhdUrl the source vhd url
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize importedFromDataVhd(String vhdUrl);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose managed disk containing data.
         */
        interface WithDataSnapshotFromManagedDisk {
            /**
             * Specifies the id of source data managed disk.
             *
             * @param managedDiskId source managed disk resource id
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize copiedFromManagedDisk(String managedDiskId);

            /**
             * Specifies the source data managed disk.
             *
             * @param managedDisk source managed disk
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize copiedFromManagedDisk(Disk managedDisk);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose managed snapshot containing data.
         */
        interface WithDataSnapshotFromSnapshot {
            /**
             * Specifies the source data managed snapshot.
             *
             * @param snapshotId snapshot resource id
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize copiedFromSnapshot(String snapshotId);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose source data disk image.
         */
        interface WithDataSnapshotFromImage {
            /**
             * Specifies id of the image containing source data disk image.
             *
             * @param imageId image resource id
             * @param diskLun lun of the disk image
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize fromImage(String imageId,
                                        int diskLun);

            /**
             * Specifies the image containing source data disk image.
             *
             * @param image the image
             * @param diskLun lun of the disk image
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize fromImage(VirtualMachineImage image, int diskLun);

            /**
             * Specifies the custom image containing source data disk image.
             *
             * @param image the image
             * @param diskLun lun of the disk image
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize fromImage(VirtualMachineCustomImage image, int diskLun);
        }

        /**
         * The stage of the managed snapshot definition that allowing to create or optionally specify size.
         */
        interface WithCreateAndSize extends WithCreate {
            /**
             * Specifies the disk size.
             *
             * @param sizeInGB the snapshot size in GB
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndSize withSizeInGB(int sizeInGB);
        }

        /**
         * The stage of the managed snapshot definition that allowing to create or optionally specify Os settings.
         */
        interface WithCreateAndOsSettings extends WithCreateAndSize {
            /**
             * Specifies the operating system type.
             *
             * @param osType operating system type
             * @return the next stage of the managed snapshot definition
             */
            WithCreateAndOsSettings withOsType(OperatingSystemTypes osType);
        }

        /**
         * The stage of the managed snapshot definition allowing to choose account type.
         */
        interface WithAccountType {
            /**
             * Specifies the account type.
             *
             * @param accountType account type
             * @return the next stage of the managed snapshot definition
             */
            WithCreate withAccountType(StorageAccountTypes accountType);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<Snapshot>,
                Resource.DefinitionWithTags<Snapshot.DefinitionStages.WithCreate>,
                WithAccountType {
        }
    }

    /**
     * Grouping of managed snapshot update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the managed snapshot update allowing to choose account type.
         */
        interface WithAccountType {
            /**
             * Specifies the account type.
             *
             * @param accountType account type
             * @return the next stage of the managed snapshot update
             */
            Update withAccountType(StorageAccountTypes accountType);
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
            Update withOsType(OperatingSystemTypes osType);
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
            UpdateStages.WithAccountType,
            UpdateStages.WithOsSettings {
    }
}
