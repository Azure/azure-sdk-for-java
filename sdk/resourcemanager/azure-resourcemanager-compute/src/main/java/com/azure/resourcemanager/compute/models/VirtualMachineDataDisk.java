// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** A managed data disk of a virtual machine. */
@Fluent
public interface VirtualMachineDataDisk extends HasInnerModel<DataDisk>, HasName, HasId {
    /**
     * Gets the size of this data disk in GB.
     *
     * @return the size of this data disk in GB
     */
    int size();

    /**
     * Gets the logical unit number assigned to this data disk.
     *
     * @return the logical unit number assigned to this data disk
     */
    int lun();

    /**
     * Gets the disk caching type.
     *
     * @return the disk caching type
     */
    CachingTypes cachingType();

    /**
     * Gets the creation method used while creating this disk.
     *
     * @return the creation method used while creating this disk
     */
    DiskCreateOptionTypes creationMethod();

    /**
     * Gets the storage account type of the disk.
     *
     * @return the storage account type of the disk
     */
    StorageAccountTypes storageAccountType();

    /**
     * Gets the disk delete options.
     *
     * @return the disk delete options
     */
    DeleteOptions deleteOptions();

    /**
     * Gets the ID of disk encryption set.
     *
     * @return the ID of disk encryption set
     */
    String diskEncryptionSetId();

    /**
     * Gets whether the write accelerator is enabled.
     *
     * @return whether the write accelerator is enabled
     */
    boolean isWriteAcceleratorEnabled();
}
