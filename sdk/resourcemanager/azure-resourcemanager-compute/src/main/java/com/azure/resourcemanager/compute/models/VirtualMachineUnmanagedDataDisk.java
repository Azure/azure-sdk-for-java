// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A native data disk of a virtual machine. */
@Fluent
public interface VirtualMachineUnmanagedDataDisk extends HasInner<DataDisk>, ChildResource<VirtualMachine> {
    /** @return the size of this data disk in GB */
    int size();

    /** @return the logical unit number assigned to this data disk */
    int lun();

    /** @return URI to the virtual hard disk backing this data disk */
    String vhdUri();

    /**
     * Gets the disk caching type.
     *
     * <p>possible values are: 'None', 'ReadOnly', 'ReadWrite'
     *
     * @return the caching type
     */
    CachingTypes cachingType();

    /**
     * Uri to the source virtual hard disk user image from which this disk was created.
     *
     * <p>null will be returned if this disk is not based on an image
     *
     * @return the URI of the source VHD image
     */
    String sourceImageUri();

    /** @return the creation method used while creating this disk */
    DiskCreateOptionTypes creationMethod();

    /** Grouping of data disk definition stages applicable as part of a virtual machine creation. */
    interface DefinitionStages {
        /**
         * The first stage of a data disk definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithDiskSource<ParentT> {
        }

        /**
         * The stage of the data disk definition allowing to choose the source.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDiskSource<ParentT> {
            /**
             * Specifies the existing source VHD of the disk.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container holding VHD file
             * @param vhdName the name of the VHD file to attach
             * @return the next stage of data disk definition
             */
            WithVhdAttachedDiskSettings<ParentT> withExistingVhd(
                String storageAccountName, String containerName, String vhdName);
            /**
             * specifies that disk needs to be created with a new VHD of given size.
             *
             * @param sizeInGB the initial disk size in GB
             * @return the next stage of data disk definition
             */
            WithNewVhdDiskSettings<ParentT> withNewVhd(int sizeInGB);

            /**
             * Specifies the image LUN identifier of the source disk image.
             *
             * @param imageLun the LUN
             * @return the next stage of data disk definition
             */
            WithFromImageDiskSettings<ParentT> fromImage(int imageLun);
        }

        /**
         * The stage that allows configure the disk based on existing VHD.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithVhdAttachedDiskSettings<ParentT> extends WithAttach<ParentT> {
            /**
             * Specifies the size in GB the disk needs to be resized.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of data disk definition
             */
            WithVhdAttachedDiskSettings<ParentT> withSizeInGB(Integer sizeInGB);

            /**
             * Specifies the logical unit number for the data disk.
             *
             * @param lun the logical unit number
             * @return the next stage of data disk definition
             */
            WithVhdAttachedDiskSettings<ParentT> withLun(Integer lun);

            /**
             * Specifies the caching type for the data disk.
             *
             * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
             * @return the next stage of data disk definition
             */
            WithVhdAttachedDiskSettings<ParentT> withCaching(CachingTypes cachingType);
        }

        /**
         * The stage that allows configure the disk based on new VHD.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithNewVhdDiskSettings<ParentT> extends WithAttach<ParentT> {
            /**
             * Specifies the logical unit number for the data disk.
             *
             * @param lun the logical unit number
             * @return the next stage of data disk definition
             */
            WithNewVhdDiskSettings<ParentT> withLun(Integer lun);

            /**
             * Specifies the caching type for the data disk.
             *
             * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
             * @return the next stage of data disk definition
             */
            WithNewVhdDiskSettings<ParentT> withCaching(CachingTypes cachingType);

            /**
             * Specifies where the VHD associated with the new blank data disk needs to be stored.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container to hold the new VHD file
             * @param vhdName the name for the new VHD file
             * @return the next stage of data disk definition
             */
            WithNewVhdDiskSettings<ParentT> storeAt(String storageAccountName, String containerName, String vhdName);
        }

        /**
         * The stage that allows configure the disk based on an image.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithFromImageDiskSettings<ParentT> extends WithAttach<ParentT> {
            /**
             * Specifies the size in GB the disk needs to be resized.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of data disk definition
             */
            WithFromImageDiskSettings<ParentT> withSizeInGB(Integer sizeInGB);

            /**
             * Specifies the caching type for the data disk.
             *
             * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
             * @return the next stage of data disk definition
             */
            WithFromImageDiskSettings<ParentT> withCaching(CachingTypes cachingType);

            /**
             * Specifies where the VHD associated with the new blank data disk needs to be stored.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container to hold the new VHD file
             * @param vhdName the name for the new VHD file
             * @return the next stage of data disk definition
             */
            WithFromImageDiskSettings<ParentT> storeAt(String storageAccountName, String containerName, String vhdName);
        }

        /**
         * The final stage of the data disk definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * The entirety of a unmanaged data disk of a virtual machine scale set definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface DefinitionWithExistingVhd<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithDiskSource<ParentT>,
            DefinitionStages.WithVhdAttachedDiskSettings<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * The entirety of a unmanaged data disk of a virtual machine scale set definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface DefinitionWithNewVhd<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithDiskSource<ParentT>,
            DefinitionStages.WithNewVhdDiskSettings<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * The entirety of a unmanaged data disk of a virtual machine scale set definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface DefinitionWithImage<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithDiskSource<ParentT>,
            DefinitionStages.WithFromImageDiskSettings<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of data disk definition stages applicable as part of a virtual machine update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a data disk definition.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithDiskSource<ParentT> {
        }

        /**
         * The stage of the data disk definition allowing to choose the source.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithDiskSource<ParentT> {
            /**
             * Specifies the existing source VHD of the disk.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container holding VHD file
             * @param vhdName the name of the VHD file to attach
             * @return the next stage of data disk definition
             */
            WithVhdAttachedDiskSettings<ParentT> withExistingVhd(
                String storageAccountName, String containerName, String vhdName);

            /**
             * specifies that disk needs to be created with a new VHD of given size.
             *
             * @param sizeInGB the initial disk size in GB
             * @return the next stage of data disk definition
             */
            WithNewVhdDiskSettings<ParentT> withNewVhd(int sizeInGB);
        }

        /**
         * The stage that allows configure the disk based on existing VHD.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithVhdAttachedDiskSettings<ParentT> extends WithAttach<ParentT> {
            /**
             * Specifies the size in GB the disk needs to be resized.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of data disk definition
             */
            WithVhdAttachedDiskSettings<ParentT> withSizeInGB(Integer sizeInGB);

            /**
             * Specifies the logical unit number for the data disk.
             *
             * @param lun the logical unit number
             * @return the next stage of data disk definition
             */
            WithVhdAttachedDiskSettings<ParentT> withLun(Integer lun);

            /**
             * Specifies the caching type for the data disk.
             *
             * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
             * @return the next stage of data disk definition
             */
            WithVhdAttachedDiskSettings<ParentT> withCaching(CachingTypes cachingType);
        }

        /**
         * The stage that allows configure the disk based on new VHD.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithNewVhdDiskSettings<ParentT> extends WithAttach<ParentT> {
            /**
             * Specifies the logical unit number for the data disk.
             *
             * @param lun the logical unit number
             * @return the next stage of data disk definition
             */
            WithNewVhdDiskSettings<ParentT> withLun(Integer lun);

            /**
             * Specifies the caching type for the data disk.
             *
             * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
             * @return the next stage of data disk definition
             */
            WithNewVhdDiskSettings<ParentT> withCaching(CachingTypes cachingType);

            /**
             * Specifies where the VHD associated with the new blank data disk needs to be stored.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container to hold the new VHD file
             * @param vhdName the name for the new VHD file
             * @return the next stage of data disk definition
             */
            WithNewVhdDiskSettings<ParentT> storeAt(String storageAccountName, String containerName, String vhdName);
        }

        /**
         * The final stage of the data disk definition.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {
        }
    }

    /**
     * The entirety of a unmanaged data disk of a virtual machine scale set update.
     *
     * @param <ParentT> the stage of the parent update to return to after attaching this definition
     */
    interface UpdateDefinitionWithExistingVhd<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithDiskSource<ParentT>,
            UpdateDefinitionStages.WithVhdAttachedDiskSettings<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * The entirety of a unmanaged data disk of a virtual machine scale set update.
     *
     * @param <ParentT> the stage of the parent update to return to after attaching this definition
     */
    interface UpdateDefinitionWithNewVhd<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithDiskSource<ParentT>,
            UpdateDefinitionStages.WithNewVhdDiskSettings<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of data disk update stages. */
    interface UpdateStages {
        /** The stage of the virtual machine data disk update allowing to set the disk size. */
        interface WithDiskSize {
            /**
             * Specifies the new size in GB for data disk.
             *
             * @param sizeInGB the disk size in GB
             * @return the next stage of data disk update
             */
            Update withSizeInGB(Integer sizeInGB);
        }

        /** The stage of the virtual machine data disk update allowing to set the disk LUN. */
        interface WithDiskLun {
            /**
             * Specifies the new logical unit number for the data disk.
             *
             * @param lun the logical unit number
             * @return the next stage of data disk update
             */
            Update withLun(Integer lun);
        }

        /** The stage of the virtual machine data disk update allowing to set the disk caching type. */
        interface WithDiskCaching {
            /**
             * Specifies the new caching type for the data disk.
             *
             * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
             * @return the next stage of data disk update
             */
            Update withCaching(CachingTypes cachingType);
        }
    }

    /** The entirety of a data disk update as part of a virtual machine update. */
    interface Update
        extends UpdateStages.WithDiskSize,
            UpdateStages.WithDiskLun,
            UpdateStages.WithDiskCaching,
            Settable<VirtualMachine.Update> {
    }
}
