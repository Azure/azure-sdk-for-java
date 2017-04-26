/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 * The implementation for {@link VirtualMachineDataDisk} interface.
 */
@LangDefinition
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
        return this.inner().managedDisk().id();
    }

    @Override
    public String name() {
        return this.inner().name();
    }
}
