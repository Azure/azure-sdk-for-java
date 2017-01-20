/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * A managed data disk of a virtual machine.
 */
@Fluent
public interface VirtualMachineDataDisk extends
        Wrapper<DataDisk>,
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
     * Gets the disk caching type.
     * <p>
     * possible values are: 'None', 'ReadOnly', 'ReadWrite'
     *
     * @return the caching type
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
