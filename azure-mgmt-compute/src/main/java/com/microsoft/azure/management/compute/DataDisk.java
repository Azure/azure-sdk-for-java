package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.api.CachingTypes;
import com.microsoft.azure.management.compute.implementation.api.DiskCreateOptionTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * A data disk of a virtual machine.
 */
public interface DataDisk extends
        Wrapper<com.microsoft.azure.management.compute.implementation.api.DataDisk>,
        ChildResource {

    /***********************************************************
     * Getters
     ***********************************************************/

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
     * uri to the source virtual hard disk user image from which this disk was created.
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
     *  and it will be used as the vhd to back the data disk
     *
     * @return disk create option
     */
    DiskCreateOptionTypes createOption();

    /**************************************************************
     * Fluent interfaces for builder pattern
     **************************************************************/

    /**
     * Container interface for all the definitions.
     *
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface Definitions<ParentT> extends
            DataDisk.DefinitionAttachNewDataDisk<ParentT>,
            DataDisk.DefinitionAttachExistingDataDisk<ParentT>,
            DataDisk.DefinitionWithStoreAt<ParentT>,
            DataDisk.DefinitionWithOptionalConfiguration<ParentT>,
            DataDisk.DefinitionAttachable<ParentT> {
    }

    /**
     * The first stage of new data disk configuration.
     *
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionAttachNewDataDisk<ParentT> {
        /**
         * Specifies the initial disk size in GB for new blank data disk.
         *
         * @param sizeInGB the disk size in GB
         * @return the stage representing optional additional settings for the attachable data disk
         */
        DefinitionWithStoreAt<ParentT> withSizeInGB(Integer sizeInGB);
    }

    /**
     * The first stage of attaching an existing disk as data disk and configuring it.
     *
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionAttachExistingDataDisk<ParentT> {
        /**
         * Specifies an existing VHD that needs to be attached to the virtual machine as data disk.
         *
         * @param storageAccountName the storage account name
         * @param containerName the name of the container holding the VHD file
         * @param vhdName the name for the VHD file
         * @return the stage representing optional additional settings for the attachable data disk
         */
        DefinitionAttachable<ParentT> from(String storageAccountName, String containerName, String vhdName);
    }

    /**
     * The stage of the data disk configuration allowing to specify location to store the VHD.
     *
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithStoreAt<ParentT> extends DefinitionAttachable<ParentT>  {
        /**
         * Specifies where the VHD associated with the new blank data disk needs to be stored.
         *
         * @param storageAccountName the storage account name
         * @param containerName the name of the container to hold the new VHD file
         * @param vhdName the name for the new VHD file
         * @return the stage representing optional additional configurations for the data disk
         */
        DefinitionAttachable<ParentT> storeAt(String storageAccountName, String containerName, String vhdName);
    }

    /**
     * The stage of the data disk configuration allowing to specify optionals common for new and
     * attachable existing data disks.
     *
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithOptionalConfiguration<ParentT> {
        /**
         * Specifies the logical unit number for the data disk.
         *
         * @param lun the logical unit number
         * @return the next stage of data disk configuration
         */
        DefinitionAttachable<ParentT> withLun(Integer lun);

        /**
         * Specifies the caching type for the data disk.
         *
         * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
         * @return the next stage of data disk configuration
         */
        DefinitionAttachable<ParentT> withCaching(CachingTypes cachingType);
    }

    /**
     * The stage of the data disk definition which contains all the minimum required inputs for the resource to be
     * attached (via {@link Attachable#attach()}), but also allows for any other optional settings to be
     * specified.
     *
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionAttachable<ParentT>  extends
            Attachable<ParentT>, DefinitionWithOptionalConfiguration<ParentT> {

    }

    /**
     * The template for a data disk update operation, containing all the settings that
     * can be modified.
     *
     * @param <ParentT> the return type of the final {@link Settable#set()}
     */
    interface Update<ParentT> extends
            Settable<ParentT> {
        /**
         * Specifies the new size in GB for data disk.
         *
         * @param sizeInGB the disk size in GB
         * @return the next stage of data disk update
         */
        Update<ParentT> withSizeInGB(Integer sizeInGB);

        /**
         * Specifies the new logical unit number for the data disk.
         *
         * @param lun the logical unit number
         * @return the next stage of data disk update
         */
        Update<ParentT> withLun(Integer lun);

        /**
         * Specifies the new caching type for the data disk.
         *
         * @param cachingType the disk caching type. Possible values include: 'None', 'ReadOnly', 'ReadWrite'
         * @return the next stage of data disk update
         */
        Update<ParentT> withCaching(CachingTypes cachingType);
    }
}
