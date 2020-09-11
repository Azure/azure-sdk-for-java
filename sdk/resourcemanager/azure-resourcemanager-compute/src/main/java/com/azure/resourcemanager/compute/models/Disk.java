// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.inner.DiskInner;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.util.Set;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure managed disk. */
@Fluent
public interface Disk extends GroupableResource<ComputeManager, DiskInner>, Refreshable<Disk>, Updatable<Disk.Update> {
    /** @return the disk SKU */
    DiskSkuTypes sku();

    /** @return the disk creation method */
    DiskCreateOption creationMethod();

    /** @return true if the disk is attached to a virtual machine, otherwise false */
    boolean isAttachedToVirtualMachine();

    /**
     * @return the resource ID of the virtual machine this disk is attached to, or null if the disk is in a detached
     *     state
     */
    String virtualMachineId();

    /** @return disk size in GB */
    int sizeInGB();

    /** @return disk size in byte */
    long sizeInByte();

    /** @return the type of the operating system on the disk */
    OperatingSystemTypes osType();

    /** @return the details of the source from which the disk is created */
    CreationSource source();

    /** @return the availability zones assigned to the disk */
    Set<AvailabilityZoneId> availabilityZones();

    /** @return the disk encryption settings */
    EncryptionSettingsCollection encryptionSettings();

    /**
     * Grants access to the disk.
     *
     * @param accessDurationInSeconds the access duration in seconds
     * @return the read-only SAS URI to the disk
     */
    String grantAccess(int accessDurationInSeconds);

    /**
     * Grants access to the disk asynchronously.
     *
     * @param accessDurationInSeconds the access duration in seconds
     * @return a representation of the deferred computation of this call returning a read-only SAS URI to the disk
     */
    Mono<String> grantAccessAsync(int accessDurationInSeconds);

    /** Revokes access granted to the disk. */
    void revokeAccess();

    /**
     * Revokes access granted to the disk asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> revokeAccessAsync();

    /** The entirety of the managed disk definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithDiskSource,
            DefinitionStages.WithWindowsDiskSource,
            DefinitionStages.WithLinuxDiskSource,
            DefinitionStages.WithData,
            DefinitionStages.WithDataDiskSource,
            DefinitionStages.WithDataDiskFromVhd,
            DefinitionStages.WithDataDiskFromUpload,
            DefinitionStages.WithDataDiskFromDisk,
            DefinitionStages.WithDataDiskFromSnapshot,
            DefinitionStages.WithStorageAccount,
            DefinitionStages.WithCreateAndSize,
            DefinitionStages.WithCreate {
    }

    /** Grouping of managed disk definition stages. */
    interface DefinitionStages {
        /** The first stage of a managed disk definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of a managed disk definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithDiskSource> {
        }

        /** The stage of a managed disk definition allowing to choose OS source or data source. */
        interface WithDiskSource extends WithWindowsDiskSource, WithLinuxDiskSource, WithData {
        }

        /** The stage of a managed disk definition allowing to choose a Windows OS source. */
        interface WithWindowsDiskSource {
            /**
             * Specifies a source Windows OS managed disk.
             *
             * @param sourceDiskId source managed disk resource ID
             * @return the next stage of the definition
             */
            WithCreateAndSize withWindowsFromDisk(String sourceDiskId);

            /**
             * Specifies a source Windows OS managed disk.
             *
             * @param sourceDisk source managed disk
             * @return the next stage of the definition
             */
            WithCreateAndSize withWindowsFromDisk(Disk sourceDisk);

            /**
             * Specifies a source Windows OS managed snapshot.
             *
             * @param sourceSnapshotId snapshot resource ID
             * @return the next stage of the definition
             */
            WithCreateAndSize withWindowsFromSnapshot(String sourceSnapshotId);

            /**
             * Specifies a source Windows OS managed snapshot.
             *
             * @param sourceSnapshot source snapshot
             * @return the next stage of the definition
             */
            WithCreateAndSize withWindowsFromSnapshot(Snapshot sourceSnapshot);

            /**
             * Specifies a source specialized or generalized Windows OS VHD.
             *
             * @param vhdUrl the source VHD URL
             * @return the next stage of the definition
             */
            WithStorageAccount withWindowsFromVhd(String vhdUrl);
        }

        /** The stage of the managed disk definition allowing to choose Linux OS source. */
        interface WithLinuxDiskSource {
            /**
             * Specifies the source Linux OS managed disk.
             *
             * @param sourceDiskId source managed disk resource ID
             * @return the next stage of the definition
             */
            WithCreateAndSize withLinuxFromDisk(String sourceDiskId);

            /**
             * Specifies the source Linux OS managed disk.
             *
             * @param sourceDisk source managed disk
             * @return the next stage of the definition
             */
            WithCreateAndSize withLinuxFromDisk(Disk sourceDisk);

            /**
             * Specifies the source Linux OS managed snapshot.
             *
             * @param sourceSnapshotId snapshot resource ID
             * @return the next stage of the definition
             */
            WithCreateAndSize withLinuxFromSnapshot(String sourceSnapshotId);

            /**
             * Specifies the source Linux OS managed snapshot.
             *
             * @param sourceSnapshot source snapshot
             * @return the next stage of the definition
             */
            WithCreateAndSize withLinuxFromSnapshot(Snapshot sourceSnapshot);

            /**
             * Specifies the source specialized or generalized Linux OS VHD.
             *
             * @param vhdUrl the source VHD URL
             * @return the next stage of the definition
             */
            WithStorageAccount withLinuxFromVhd(String vhdUrl);
        }

        /** The stage of the managed disk definition that specifies it hold data. */
        interface WithData {
            /**
             * Begins definition of managed disk containing data.
             *
             * @return the next stage of the definition
             */
            WithDataDiskSource withData();
        }

        /** The stage of the managed disk definition allowing to choose data source. */
        interface WithDataDiskSource
            extends WithDataDiskFromVhd, WithDataDiskFromUpload, WithDataDiskFromDisk, WithDataDiskFromSnapshot {
            /**
             * Specifies the disk size for an empty disk.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the definition
             */
            WithCreate withSizeInGB(int sizeInGB);
        }

        /** The stage of the managed disk definition allowing to choose source data disk VHD. */
        interface WithDataDiskFromVhd {
            /**
             * Specifies the source data VHD.
             *
             * @param vhdUrl the source VHD URL
             * @return the next stage of the definition
             */
            WithStorageAccount fromVhd(String vhdUrl);
        }

        /** The stage of the managed disk definition allowing to create disk from upload. */
        interface WithDataDiskFromUpload {
            /**
             * Gets or sets if createOption is Upload, this is the size of the contents of the upload including the VHD
             * footer. This value should be between 20 (20 MiB) and 33554432 bytes (32 TiB).
             *
             * @param uploadSizeInMB The size of the contents of the upload in MB
             * @return The next stage of the definition.
             */
            WithCreate withUploadSizeInMB(long uploadSizeInMB);
        }

        /** The stage of the managed disk definition allowing to choose managed disk containing data. */
        interface WithDataDiskFromDisk {
            /**
             * Specifies the ID of source data managed disk.
             *
             * @param managedDiskId source managed disk resource ID
             * @return the next stage of the definition
             */
            WithCreateAndSize fromDisk(String managedDiskId);

            /**
             * Specifies the source data managed disk.
             *
             * @param managedDisk source managed disk
             * @return the next stage of the definition
             */
            WithCreateAndSize fromDisk(Disk managedDisk);
        }

        /** The stage of the managed disk definition allowing to choose managed snapshot containing data. */
        interface WithDataDiskFromSnapshot {
            /**
             * Specifies the source data managed snapshot.
             *
             * @param snapshotId snapshot resource ID
             * @return the next stage of the definition
             */
            WithCreateAndSize fromSnapshot(String snapshotId);

            /**
             * Specifies the source data managed snapshot.
             *
             * @param snapshot snapshot resource
             * @return the next stage of the definition
             */
            WithCreateAndSize fromSnapshot(Snapshot snapshot);
        }

        /** The stage of the managed disk definition allowing to choose source operating system image. */
        interface WithOSDiskFromImage {
            /**
             * Specifies the ID of an image containing the operating system.
             *
             * @param imageId image resource ID
             * @param osType operating system type
             * @return the next stage of the definition
             */
            WithCreateAndSize fromImage(String imageId, OperatingSystemTypes osType);

            /**
             * Specifies an image containing the operating system.
             *
             * @param image the image
             * @return the next stage of the definition
             */
            WithCreateAndSize fromImage(VirtualMachineImage image);

            /**
             * Specifies a custom image containing the operating system.
             *
             * @param image the image
             * @return the next stage of the definition
             */
            WithCreateAndSize fromImage(VirtualMachineCustomImage image);
        }
        /** The stage of the managed disk definition allowing to choose source data disk image. */
        interface WithDataDiskFromImage {
            /**
             * Specifies the ID of an image containing source data disk image.
             *
             * @param imageId image resource ID
             * @param diskLun LUN of the disk image
             * @return the next stage of the definition
             */
            WithCreateAndSize fromImage(String imageId, int diskLun);

            /**
             * Specifies an image containing source data disk image.
             *
             * @param image the image
             * @param diskLun LUN of the disk image
             * @return the next stage of the definition
             */
            WithCreateAndSize fromImage(VirtualMachineImage image, int diskLun);

            /**
             * Specifies a custom image containing a source data disk image.
             *
             * @param image the image
             * @param diskLun LUN of the disk image
             * @return the next stage of the definition
             */
            WithCreateAndSize fromImage(VirtualMachineCustomImage image, int diskLun);
        }

        /**
         * The stage of the managed disk definition allowing to specify storage account that contains disk information.
         *
         * <p>It is mandatory in import option.
         */
        interface WithStorageAccount {
            /**
             * Specifies the storage account id.
             *
             * @param storageAccountId the storage account id
             * @return the next stage of the definition
             */
            WithCreateAndSize withStorageAccountId(String storageAccountId);

            /**
             * Specifies the storage account name in same resource group.
             *
             * @param storageAccountName the storage account name in same resource group
             * @return the next stage of the definition
             */
            WithCreateAndSize withStorageAccountName(String storageAccountName);

            /**
             * Specifies the storage account.
             *
             * @param account the storage account
             * @return the next stage of the definition
             */
            WithCreateAndSize withStorageAccount(StorageAccount account);
        }

        /** The stage of the managed disk definition allowing to specify availability zone. */
        interface WithAvailabilityZone {
            /**
             * Specifies the availability zone for the managed disk.
             *
             * @param zoneId the zone identifier.
             * @return the next stage of the definition
             */
            WithCreate withAvailabilityZone(AvailabilityZoneId zoneId);
        }

        /** The stage of the managed disk definition allowing to create the disk or optionally specify size. */
        interface WithCreateAndSize extends WithCreate {
            /**
             * Specifies the disk size.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the definition
             */
            WithCreate withSizeInGB(int sizeInGB);
        }

        /** The stage of the managed disk definition allowing to choose account type. */
        interface WithSku {
            /**
             * Specifies the SKU.
             *
             * @param sku the SKU
             * @return the next stage of the definition
             */
            WithCreate withSku(DiskSkuTypes sku);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<Disk>,
                Resource.DefinitionWithTags<Disk.DefinitionStages.WithCreate>,
                WithSku,
                WithAvailabilityZone {

            /**
             * Begins creating the disk resource.
             *
             * @return the accepted create operation
             */
            Accepted<Disk> beginCreate();
        }
    }

    /** Grouping of managed disk update stages. */
    interface UpdateStages {
        /** The stage of the managed disk update allowing to choose the SKU type. */
        interface WithSku {
            /**
             * Specifies the SKU.
             *
             * @param sku a SKU
             * @return the next stage of the update
             */
            Update withSku(DiskSkuTypes sku);
        }

        /** The stage of the managed disk definition allowing to specify new size. */
        interface WithSize {
            /**
             * Specifies the disk size.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of the update
             */
            Update withSizeInGB(int sizeInGB);
        }

        /** The stage of the managed disk update allowing to specify OS settings. */
        interface WithOSSettings {
            /**
             * Specifies the operating system.
             *
             * @param osType operating system type
             * @return the next stage of the update
             */
            Update withOSType(OperatingSystemTypes osType);
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<Disk>,
            Resource.UpdateWithTags<Disk.Update>,
            UpdateStages.WithSku,
            UpdateStages.WithSize,
            UpdateStages.WithOSSettings {
    }
}
