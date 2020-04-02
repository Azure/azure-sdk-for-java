/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.management.compute.CachingTypes;
import com.azure.management.compute.DataDisk;
import com.azure.management.compute.DiskCreateOptionTypes;
import com.azure.management.compute.StorageAccountTypes;
import com.azure.management.compute.VirtualMachineDataDisk;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.resources.fluentcore.utils.Utils;

/**
 * The implementation for {@link VirtualMachineDataDisk} interface.
 */
class VirtualMachineDataDiskImpl
        extends WrapperImpl<DataDisk>
        implements VirtualMachineDataDisk  {

    VirtualMachineDataDiskImpl(DataDisk dataDiskInner) {
        super(dataDiskInner);
    }

    @Override
    public int size() {
        return Utils.toPrimitiveInt(this.inner().diskSizeGB());
    }

    @Override
    public int lun() {
        return this.inner().lun();
    }

    @Override
    public CachingTypes cachingType() {
        return this.inner().caching();
    }

    @Override
    public DiskCreateOptionTypes creationMethod() {
        return this.inner().createOption();
    }

    @Override
    public StorageAccountTypes storageAccountType() {
        if (this.inner().managedDisk() == null) {
            return null;
        }
        return this.inner().managedDisk().storageAccountType();
    }

    @Override
    public String id() {
        if (this.inner().managedDisk() == null) {
            return null;
        }
        return this.inner().managedDisk().getId();
    }

    @Override
    public String name() {
        return this.inner().name();
    }
}
