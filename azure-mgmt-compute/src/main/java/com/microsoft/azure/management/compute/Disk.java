/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
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
        GroupableResource<ComputeManager>,
        Refreshable<Disk>,
        Wrapper<DiskInner>,
        Updatable<Disk.Update> {
    /**
     * @return the disk sku
     */
    DiskSkuTypes sku();

    /**
     * @return the disk creation method
     */
    DiskCreateOption creationMethod();

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
     * @return the type of operating system in the disk
     */
    OperatingSystemTypes osType();

    /**
     * @return the details of the source from which disk is created
     */
    CreationSource source();

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
            DefinitionStages.WithDiskSource,
            DefinitionStages.WithWindowsDiskSource,
            DefinitionStages.WithLinuxDiskSource,
            DefinitionStages.WithData,
            DefinitionStages.WithDataDiskSource,
            DefinitionStages.WithDataDiskFromVhd,
            DefinitionStages.WithDataDiskFromDisk,
            DefinitionStages.WithDataDiskFromSnapshot,
            DefinitionStages.WithCreateAndSize,
            DefinitionStages.WithCreate {
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
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithDiskSource> {
        }

        /**
         * The stage of the managed disk definition allowing to choose OS source or data source.
         */
        interface WithDiskSource
                extends
                WithWindowsDiskSource,
                WithLinuxDiskSource,
                WithData {
        }

        /**
         *  The stage of the managed disk definition allowing to choose Windows OS source.
         */
        interface WithWindowsDiskSource {
            /**
             * Specifies the source Windows OS managed disk.
             *
             * @param sourceDiskId source managed disk resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withWindowsFromDisk(String sourceDiskId);

            /**
             * Specifies the source Windows OS managed disk.
             *
             * @param sourceDisk source managed disk
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withWindowsFromDisk(Disk sourceDisk);

            /**
             * Specifies the source Windows OS managed snapshot.
             *
             * @param sourceSnapshotId snapshot resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withWindowsFromSnapshot(String sourceSnapshotId);

            /**
             * Specifies the source Windows OS managed snapshot.
             *
             * @param sourceSnapshot source snapshot
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withWindowsFromSnapshot(Snapshot sourceSnapshot);

            /**
             * Specifies the source specialized or generalized Windows OS vhd.
             *
             * @param vhdUrl the source vhd url
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withWindowsFromVhd(String vhdUrl);
        }

        /**
         *  The stage of the managed disk definition allowing to choose Linux OS source.
         */
        interface WithLinuxDiskSource {
            /**
             * Specifies the source Linux OS managed disk.
             *
             * @param sourceDiskId source managed disk resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withLinuxFromDisk(String sourceDiskId);

            /**
             * Specifies the source Linux OS managed disk.
             *
             * @param sourceDisk source managed disk
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withLinuxFromDisk(Disk sourceDisk);

            /**
             * Specifies the source Linux OS managed snapshot.
             *
             * @param sourceSnapshotId snapshot resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withLinuxFromSnapshot(String sourceSnapshotId);

            /**
             * Specifies the source Linux OS managed snapshot.
             *
             * @param sourceSnapshot source snapshot
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withLinuxFromSnapshot(Snapshot sourceSnapshot);

            /**
             * Specifies the source specialized or generalized Linux OS vhd.
             *
             * @param vhdUrl the source vhd url
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize withLinuxFromVhd(String vhdUrl);
        }

        /**
         * The stage of the managed disk definition that specifies it hold data.
         */
        interface WithData {
            /**
             * Begins definition of managed disk containing data.
             *
             * @return the next stage of the managed disk definition
             */
            WithDataDiskSource withData();
        }

        /**
         * The stage of the managed disk definition allowing to choose data source.
         */
        interface WithDataDiskSource extends
                WithDataDiskFromVhd,
                WithDataDiskFromDisk,
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
         * The stage of the managed disk definition allowing to choose source data disk vhd.
         */
        interface WithDataDiskFromVhd {
            /**
             * Specifies the source data vhd.
             *
             * @param vhdUrl the source vhd url
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromVhd(String vhdUrl);
        }

        /**
         * The stage of the managed disk definition allowing to choose managed disk containing data.
         */
        interface WithDataDiskFromDisk {
            /**
             * Specifies the id of source data managed disk.
             *
             * @param managedDiskId source managed disk resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromDisk(String managedDiskId);

            /**
             * Specifies the source data managed disk.
             *
             * @param managedDisk source managed disk
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromDisk(Disk managedDisk);
        }

        /**
         * The stage of the managed disk definition allowing to choose managed snapshot containing data.
         */
        interface WithDataDiskFromSnapshot {
            /**
             * Specifies the source data managed snapshot.
             *
             * @param snapshotId snapshot resource id
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromSnapshot(String snapshotId);

            /**
             * Specifies the source data managed snapshot.
             *
             * @param snapshot snapshot resource
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromSnapshot(Snapshot snapshot);
        }

        /**
         * The stage of the managed disk definition allowing to choose source operating system image.
         */
        interface WithOsDiskFromImage {
            /**
             * Specifies id of the image containing operating system.
             *
             * @param imageId image resource id
             * @param osType operating system type
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(String imageId,
                                        OperatingSystemTypes osType);

            /**
             * Specifies the image containing operating system.
             *
             * @param image the image
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(VirtualMachineImage image);

            /**
             * Specifies the custom image containing operating system.
             *
             * @param image the image
             * @return the next stage of the managed disk definition
             */
            WithCreateAndSize fromImage(VirtualMachineCustomImage image);
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
         * The stage of the managed disk definition allowing to choose account type.
         */
        interface WithSku {
            /**
             * Specifies the sku.
             *
             * @param sku the sku
             * @return the next stage of the managed disk definition
             */
            WithCreate withSku(DiskSkuTypes sku);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<Disk>,
                Resource.DefinitionWithTags<Disk.DefinitionStages.WithCreate>,
                WithSku {
        }
    }

    /**
     * Grouping of managed disk update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the managed disk update allowing to choose sku type.
         */
        interface WithSku {
            /**
             * Specifies the sku.
             *
             * @param sku the sku
             * @return the next stage of the managed disk update
             */
            Update withSku(DiskSkuTypes sku);
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
            Appliable<Disk>,
            Resource.UpdateWithTags<Disk.Update>,
            UpdateStages.WithSku,
            UpdateStages.WithSize,
            UpdateStages.WithOsSettings {
    }
}
