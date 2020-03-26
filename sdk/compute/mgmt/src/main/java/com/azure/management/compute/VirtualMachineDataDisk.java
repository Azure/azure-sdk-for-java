/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasId;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * A managed data disk of a virtual machine.
 */
@Fluent
public interface VirtualMachineDataDisk extends
        HasInner<DataDisk>,
        HasName,
        HasId {
    /**
     * @return the size of this data disk in GB
     */
    int size();

    /**
     * @return the logical unit number assigned to this data disk
     */
    int lun();

    /**
     * @return the disk caching type
     */
    CachingTypes cachingType();

    /**
     * @return the creation method used while creating this disk
     */
    DiskCreateOptionTypes creationMethod();

    /**
     * @return the storage account type of the disk
     */
    StorageAccountTypes storageAccountType();
}
