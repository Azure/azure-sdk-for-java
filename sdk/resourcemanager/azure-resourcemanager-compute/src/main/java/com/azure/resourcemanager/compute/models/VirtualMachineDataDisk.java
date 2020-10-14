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
    /** @return the size of this data disk in GB */
    int size();

    /** @return the logical unit number assigned to this data disk */
    int lun();

    /** @return the disk caching type */
    CachingTypes cachingType();

    /** @return the creation method used while creating this disk */
    DiskCreateOptionTypes creationMethod();

    /** @return the storage account type of the disk */
    StorageAccountTypes storageAccountType();
}
