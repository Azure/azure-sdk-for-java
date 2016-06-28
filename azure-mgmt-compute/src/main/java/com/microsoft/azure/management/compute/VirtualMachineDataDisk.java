package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * A data disk of a virtual machine.
 */
public interface VirtualMachineDataDisk extends
        Wrapper<DataDisk>,
        ChildResource {

    // getters

    /**
     * @return the size of this data disk in GB
     */
    int size();

    /**
     * @return the logical unit number assigned to this data disk
     */
    int lun();

    /**
     * @return uri to the virtual hard disk backing this data disk
     */
    String vhdUri();

    /**
     * Gets the disk caching type.
     * <p>
     * possible values are: 'None', 'ReadOnly', 'ReadWrite'
     *
     * @return the caching type
     */
    CachingTypes cachingType();

    /**
     * Uri to the source virtual hard disk user image from which this disk was created.
     * <p>
     * null will be returned if this disk is not based on an image
     *
     * @return the uri of the source vhd image
     */
    String sourceImageUri();

    /**
     * Gets the create option used while creating this disk.
     * <p>
     * Possible values include: 'fromImage', 'empty', 'attach'
     * 'fromImage' - if data disk was created from a user image
     * 'attach' - if an existing vhd was usd to back the data disk
     * 'empty' - if the disk was created as an empty disk
     *  when disk is created using 'fromImage' option, a copy of user image vhd will be created first
     *  and it will be used as the vhd to back the data disk.
     *
     * @return disk create option
     */
    DiskCreateOptionTypes createOption();

    // fluent (setters)

    /**
     * Grouping of data disk definition stages applicable as part of a virtual machine creation.
     */
    interface DefinitionStages {
        /**
         * The first stage of a  data disk definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT>
                extends WithDataDisk<ParentT> {
        }

        /**
         * The stage allowing to choose configuring new or existing data disk.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithDataDisk<ParentT>
                extends AttachNewDataDisk<ParentT>, AttachExistingDataDisk<ParentT> {
        }

        /**
         * The first stage of new data disk configuration.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface AttachNewDataDisk<ParentT> {
            /**
             * Specifies the initial disk size in GB for new blank data disk.
             *
             * @param sizeInGB the disk size in GB
             * @return the stage representing optional additional settings for the attachable data disk
             */
            WithStoreAt<ParentT> withSizeInGB(Integer sizeInGB);
        }

        /**
         * The stage of the new data disk configuration allowing to specify location to store the VHD.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithStoreAt<ParentT> extends WithAttach<ParentT>  {
            /**
             * Specifies where the VHD associated with the new blank data disk needs to be stored.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container to hold the new VHD file
             * @param vhdName the name for the new VHD file
             * @return the stage representing optional additional configurations for the data disk
             */
            WithAttach<ParentT> storeAt(String storageAccountName, String containerName, String vhdName);
        }

        /**
         * The first stage of attaching an existing disk as data disk and configuring it.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface AttachExistingDataDisk<ParentT> {
            /**
             * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container holding the VHD file
             * @param vhdName the name for the VHD file
             * @return the stage representing optional additional settings for the attachable data disk
             */
            WithAttach<ParentT> from(String storageAccountName, String containerName, String vhdName);
        }

        /** The final stage of the data disk definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the data disk definition
         * can be attached to the parent virtual machine definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {
            /**
             * Specifies the logical unit number for the data disk.
             *
             * @param lun the logical unit number
             * @return the next stage of data disk definition
             */
            WithAttach<ParentT> withLun(Integer lun);

            /**
             * Specifies the caching type for the data disk.
             *
             * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
             * @return the next stage of data disk definition
             */
            WithAttach<ParentT> withCaching(CachingTypes cachingType);
        }
    }

    /**
     * The entirety of a data disk definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithStoreAt<ParentT> {
    }

    /**
     * Grouping of data disk definition stages applicable as part of a virtual machine update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a  data disk definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT>
                extends WithDataDisk<ParentT> {
        }

        /**
         * The stage allowing to choose configuring new or existing data disk.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithDataDisk<ParentT>
                extends AttachNewDataDisk<ParentT>, AttachExistingDataDisk<ParentT> {
        }

        /**
         * The first stage of new data disk configuration.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface AttachNewDataDisk<ParentT> {
            /**
             * Specifies the initial disk size in GB for new blank data disk.
             *
             * @param sizeInGB the disk size in GB
             * @return the stage representing optional additional settings for the attachable data disk
             */
            WithStoreAt<ParentT> withSizeInGB(Integer sizeInGB);
        }

        /**
         * The stage of the new data disk configuration allowing to specify location to store the VHD.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithStoreAt<ParentT> extends WithAttach<ParentT>  {
            /**
             * Specifies where the VHD associated with the new blank data disk needs to be stored.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container to hold the new VHD file
             * @param vhdName the name for the new VHD file
             * @return the stage representing optional additional configurations for the data disk
             */
            WithAttach<ParentT> storeAt(String storageAccountName, String containerName, String vhdName);
        }

        /**
         * The first stage of attaching an existing disk as data disk and configuring it.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface AttachExistingDataDisk<ParentT> {
            /**
             * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
             *
             * @param storageAccountName the storage account name
             * @param containerName the name of the container holding the VHD file
             * @param vhdName the name for the VHD file
             * @return the stage representing optional additional settings for the attachable data disk
             */
            WithAttach<ParentT> from(String storageAccountName, String containerName, String vhdName);
        }

        /** The final stage of the data disk definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the data disk definition
         * can be attached to the parent virtual machine definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
            /**
             * Specifies the logical unit number for the data disk.
             *
             * @param lun the logical unit number
             * @return the next stage of data disk definition
             */
            WithAttach<ParentT> withLun(Integer lun);

            /**
             * Specifies the caching type for the data disk.
             *
             * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
             * @return the next stage of data disk definition
             */
            WithAttach<ParentT> withCaching(CachingTypes cachingType);
        }
    }

    /** The entirety of a data disk definition as part of a virtual machine update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>  extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithStoreAt<ParentT> {
    }

    /**
     * Grouping of data disk update stages.
     */
    interface UpdateStages {
        /**
         * Specifies the new size in GB for data disk.
         *
         * @param sizeInGB the disk size in GB
         * @return the next stage of data disk update
         */
        Update withSizeInGB(Integer sizeInGB);

        /**
         * Specifies the new logical unit number for the data disk.
         *
         * @param lun the logical unit number
         * @return the next stage of data disk update
         */
        Update withLun(Integer lun);

        /**
         * Specifies the new caching type for the data disk.
         *
         * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
         * @return the next stage of data disk update
         */
        Update withCaching(CachingTypes cachingType);
    }

    /**
     * The entirety of a data disk update as part of a virtual machine update.
     */
    interface Update extends
            UpdateStages,
            Settable<VirtualMachine.Update> {
    }
}