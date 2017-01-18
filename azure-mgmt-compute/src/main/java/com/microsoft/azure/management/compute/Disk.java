/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.DiskInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure managed disk.
 */
@Fluent
public interface Disk extends
        GroupableResource,
        Refreshable<Disk>,
        Wrapper<DiskInner>,
        Updatable<Disk.Update> {
    /**
     * @return the disk account type
     */
    StorageAccountTypes accountType();

    /**
     * @return the disk create option
     */
    DiskCreateOption createOption();

    /**
     * @return true if the disk is attached to a virtual machine, false if is
     * in detached state
     */
    boolean isAttachedToVirtualMachine();

    /**
     * @return resource id of the virtual machine this disk is attached to, null
     * if the disk is in detached state
     */
    String virtualMachineId();

    /**
     * @return disk size in GB
     */
    int sizeInGB();

    /**
     * @return the state of the operating system in the disk
     */
    OperatingSystemStateTypes osState();

    /**
     * @return the type of operating system in the disk
     */
    OperatingSystemTypes osType();

    /**
     * @return the details of the source from which disk is created
     */
    DiskSource source();

    /**
     * Grants access to the disk.
     *
     * @param accessDurationInSeconds the access duration in seconds
     * @return the readonly SAS uri to the disk
     */
    String grantAccess(int accessDurationInSeconds);

    /**
     * Revoke access granted to the disk.
     */
    void revokeAccess();

    /**
     * The entirety of the managed disk definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithDiskType,
            DefinitionStages.WithOsDisk,
            DefinitionStages.WithDataDisk,
            DefinitionStages.WithCreateAndOsSettings {
    }

    /**
     * Grouping of managed disk definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a managed disk definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the managed disk definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithDiskType> {
        }

        /**
         * The stage of the managed disk definition allowing to choose disk type.
         */
        interface WithDiskType {
            /**
             * Begins definition of managed disk holding operating system disk.
             *
             * @return the next stage of the managed disk definition
             */
            WithOsDisk withOs();

            /**
             * Begins definition of managed disk holding data disk.
             *
             * @return the next stage of the managed disk definition
             */
            WithDataDisk withData();
        }

        /**
         * The stage of the managed disk definition allowing to choose Os disk source.
         */
        interface WithOsDisk extends
                WithOsDiskFromImage,
                WithOsDiskFromVhd,
                WithOsDiskFromManagedDisk,
                WithOsDiskFromSnapshot {
        }

        /**
         * The stage of the managed disk definition allowing to choose data disk source.
         */
        interface WithDataDisk extends
                WithDataDiskFromImage,
                WithDataDiskFromVhd,
                WithDataDiskFromManagedDisk,
                WithDataDiskFromSnapshot {
            /**
             * Specifies the disk size for an empty disk.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the managed disk definition
             */
            WithCreate withSizeInGB(int sizeInGB);
        }

        /**
         * The stage of the managed disk definition allowing to choose source Os disk image.
         */
        interface WithOsDiskFromImage {
            /**
             * Specifies id of the image containing source Os disk image.
             *
             * @param imageId image resource id
             * @param osType operating system type
             * @param osState operating system state
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(String imageId,
                                        OperatingSystemTypes osType,
                                        OperatingSystemStateTypes osState);

            /**
             * Specifies the image containing source Os disk image.
             *
             * @param image the image
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(VirtualMachineImage image);

            /**
             * Specifies the custom image containing source Os disk image.
             *
             * @param image the image
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(VirtualMachineCustomImage image);
        }

        /**
         * The stage of the managed disk definition allowing to choose source Os disk vhd.
         */
        interface WithOsDiskFromVhd {
            /**
             * Specifies the source specialized Os vhd for os disk.
             *
             * @param vhdUrl the source vhd url
             * @param osType operating system type
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize importedFromSpecializedOsVhd(String vhdUrl, OperatingSystemTypes osType);

            /**
             * Specifies the source generalized Os vhd for os disk.
             *
             * @param vhdUrl the source vhd url
             * @param osType operating system type
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize importedFromGeneralizedOsVhd(String vhdUrl, OperatingSystemTypes osType);
        }

        /**
         * The stage of the managed disk definition allowing to choose source managed disk for os disk.
         */
        interface WithOsDiskFromManagedDisk {
            /**
             * Specifies the source managed disk for os disk.
             *
             * @param managedDiskId source managed disk resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndOsSettings copiedFromManagedDisk(String managedDiskId);

            /**
             * Specifies the source managed disk for Os disk.
             *
             * @param managedDisk source managed disk
             * @return the next stage of the managed disk definition
             */
            WithCreateAndOsSettings copiedFromManagedDisk(Disk managedDisk);
        }

        /**
         * The stage of the managed disk definition allowing to choose source snapshot for os disk.
         */
        interface WithOsDiskFromSnapshot {
            /**
             * Specifies the source snapshot for os disk.
             *
             * @param snapshotId snapshot resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndOsSettings copiedFromSnapshot(String snapshotId);
        }

        /**
         * The stage of the managed disk definition allowing to choose source data disk image.
         */
        interface WithDataDiskFromImage {
            /**
             * Specifies id of the image containing source data disk image.
             *
             * @param imageId image resource id
             * @param diskLun lun of the disk image
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(String imageId,
                                        int diskLun);

            /**
             * Specifies the image containing source data disk image.
             *
             * @param image the image
             * @param diskLun lun of the disk image
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(VirtualMachineImage image, int diskLun);

            /**
             * Specifies the custom image containing source data disk image.
             *
             * @param image the image
             * @param diskLun lun of the disk image
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(VirtualMachineCustomImage image, int diskLun);
        }

        /**
         * The stage of the managed disk definition allowing to choose source data disk vhd.
         */
        interface WithDataDiskFromVhd {
            /**
             * Specifies the source data Vhd.
             *
             * @param vhdUrl the source vhd url
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize importedFromDataVhd(String vhdUrl);
        }

        /**
         * The stage of the managed disk definition allowing to choose source data disk managed disk.
         */
        interface WithDataDiskFromManagedDisk {
            /**
             * Specifies the source managed disk for data disk.
             *
             * @param managedDiskId source managed disk resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize copiedFromManagedDisk(String managedDiskId);

            /**
             * Specifies the source managed disk for data disk.
             *
             * @param managedDisk source managed disk
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize copiedFromManagedDisk(Disk managedDisk);
        }

        /**
         * The stage of the managed disk definition allowing to choose source data disk snapshot.
         */
        interface WithDataDiskFromSnapshot {
            /**
             * Specifies the source snapshot for data disk.
             *
             * @param snapshotId snapshot resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize copiedFromSnapshot(String snapshotId);
        }

        /**
         * The stage of the managed disk definition that allowing to create or optionally specify size.
         */
        interface WithCreateAndSize extends WithCreate {
            /**
             * Specifies the disk size.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withSizeInGB(int sizeInGB);
        }

        /**
         * The stage of the managed disk definition that allowing to create or optionally specify Os settings.
         */
        interface WithCreateAndOsSettings extends WithCreateAndSize {
            /**
             * Specifies the operating system type.
             *
             * @param osType operating system type
             * @return the next stage of the managed disk definition
             */
            WithCreateAndOsSettings withOsType(OperatingSystemTypes osType);

            /**
             * Specifies the operating system state.
             *
             * @param osState operating system state
             * @return the next stage of the managed disk definition
             */
            WithCreateAndOsSettings withOsState(OperatingSystemStateTypes osState);
        }

        /**
         * The stage of the managed disk definition allowing to choose account type.
         */
        interface WithAccountType {
            /**
             * Specifies the account type.
             *
             * @param accountType account type
             * @return the next stage of the managed disk definition
             */
            WithCreate withAccountType(StorageAccountTypes accountType);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<Disk>,
                Resource.DefinitionWithTags<Disk.DefinitionStages.WithCreate>,
                WithAccountType {
        }
    }

    /**
     * Grouping of managed disk update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the managed disk update allowing to choose account type.
         */
        interface WithAccountType {
            /**
             * Specifies the account type.
             *
             * @param accountType account type
             * @return the next stage of the managed disk update
             */
            Update withAccountType(StorageAccountTypes accountType);
        }

        /**
         * The stage of the managed disk definition allowing to specify new size.
         */
        interface WithSize {
            /**
             * Specifies the disk size.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the managed disk update
             */
            Update withSizeInGB(int sizeInGB);
        }

        /**
         * The stage of the managed disk update allowing to specify Os settings.
         */
        interface WithOsSettings {
            /**
             * Specifies the operating system type.
             *
             * @param osType operating system type
             * @return the next stage of the managed disk update
             */
            Update withOsType(OperatingSystemTypes osType);

            /**
             * Specifies the operating system state.
             *
             * @param osState operating system state
             * @return the next stage of the managed disk update
             */
            Update withOsState(OperatingSystemStateTypes osState);
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Disk.Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<Disk>,
            Resource.UpdateWithTags<Disk.Update>,
            UpdateStages.WithAccountType,
            UpdateStages.WithSize,
            UpdateStages.WithOsSettings {
    }
}
